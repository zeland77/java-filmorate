package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.DirectorRowMapper;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.*;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        JdbcFilmRepository.class,
        JdbcDirectorRepository.class,
        FilmRowMapper.class,
        DirectorRowMapper.class,
        JdbcUserRepository.class,
        UserRowMapper.class
})
public class AddDirectorTest {
    private final DirectorRepository directorRepository;
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;

    @Test
    void shouldAddDirector() {
        Director director = new Director();
        director.setName("Тарантино");

        Director saved = directorRepository.addDirector(director);

        assertThat(saved.getId()).isNotNull().isPositive();
        assertThat(saved.getName()).isEqualTo("Тарантино");

        Director found = directorRepository.findDirById(saved.getId());
        assertThat(found.getName()).isEqualTo("Тарантино");
    }

    @Test
    void shouldNotAddDirectorWithSameName() {
        Director first = new Director();
        first.setName("Нолан");
        directorRepository.addDirector(first);

        Director second = new Director();
        second.setName("Нолан");

        assertThatThrownBy(() -> directorRepository.addDirector(second))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldNotAddDirectorWithSameNameIgnoreCase() {
        Director first = new Director();
        first.setName("Спилберг");
        directorRepository.addDirector(first);

        Director second = new Director();
        second.setName("спилберг");

        assertThatThrownBy(() -> directorRepository.addDirector(second))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldFindDirectorById() {
        Director director = new Director();
        director.setName("Кэмерон");
        Director saved = directorRepository.addDirector(director);

        Director found = directorRepository.findDirById(saved.getId());

        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getName()).isEqualTo("Кэмерон");
    }

    @Test
    void shouldThrowWhenDirectorNotFound() {
        assertThatThrownBy(() -> directorRepository.findDirById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldReturnEmptyListWhenNoDirectors() {
        assertThat(directorRepository.findAllDirectors()).isEmpty();
    }

    @Test
    void shouldReturnAllDirectorsSortedById() {
        Director first = new Director();
        first.setName("А");
        Director second = new Director();
        second.setName("Б");

        Director savedFirst = directorRepository.addDirector(first);
        Director savedSecond = directorRepository.addDirector(second);

        assertThat(directorRepository.findAllDirectors())
                .extracting(Director::getId)
                .containsExactly(savedFirst.getId(), savedSecond.getId());

        assertThat(directorRepository.findAllDirectors())
                .extracting(Director::getName)
                .containsExactly("А", "Б");
    }

    @Test
    void shouldUpdateDirectorName() {
        Director director = new Director();
        director.setName("Старое имя");
        Director saved = directorRepository.addDirector(director);

        saved.setName("Новое имя");
        Director updated = directorRepository.updateDirector(saved);

        assertThat(updated.getName()).isEqualTo("Новое имя");
        assertThat(directorRepository.findDirById(saved.getId()).getName())
                .isEqualTo("Новое имя");
    }

    @Test
    void shouldUpdateDirectorWithSameName() {
        Director director = new Director();
        director.setName("Нолан");
        Director saved = directorRepository.addDirector(director);

        saved.setName("Нолан");

        assertThat(directorRepository.updateDirector(saved).getName())
                .isEqualTo("Нолан");
    }

    @Test
    void shouldNotUpdateToAnotherDirectorsName() {
        Director first = new Director();
        first.setName("Нолан");
        directorRepository.addDirector(first);

        Director second = new Director();
        second.setName("Спилберг");
        Director savedSecond = directorRepository.addDirector(second);

        savedSecond.setName("Нолан");

        assertThatThrownBy(() -> directorRepository.updateDirector(savedSecond))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void shouldThrowWhenUpdatingUnknownDirector() {
        Director director = new Director();
        director.setId(999L);
        director.setName("Никто");

        assertThatThrownBy(() -> directorRepository.updateDirector(director))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldDeleteDirector() {
        Director director = new Director();
        director.setName("Удаляемый");
        Director saved = directorRepository.addDirector(director);

        directorRepository.deleteDirector(saved.getId());

        assertThatThrownBy(() -> directorRepository.findDirById(saved.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldNotThrowWhenDeletingUnknownDirector() {
        assertThatCode(() -> directorRepository.deleteDirector(999L))
                .doesNotThrowAnyException();
    }

    @Test
    void shouldReturnFilmsByDirectorSortedByYear() {
        Director director = new Director();
        director.setName("Режиссёр");
        directorRepository.addDirector(director);

        Film oldFilm = createFilm("old", LocalDate.of(1999, 6, 20));
        oldFilm.addDirector(director);
        Film newFilm = createFilm("new", LocalDate.of(1999, 6, 21));
        newFilm.addDirector(director);

        oldFilm = filmRepository.create(newFilm).get();
        newFilm = filmRepository.create(oldFilm).get();

        assertThat(filmRepository.findFilmsByDirector(director.getId(), "year"))
                .extracting(Film::getId).containsExactly(oldFilm.getId(), newFilm.getId());
        assertThat(filmRepository.findFilmsByDirector(director.getId(), "year"))
                .extracting(Film::getName).containsExactly(oldFilm.getName(), newFilm.getName());
        assertThat(filmRepository.findFilmsByDirector(director.getId(), "year"))
                .extracting(Film::getDirectors)
                .containsOnly(Set.of(director));
    }

    @Test
    void shouldReturnFilmsByDirectorSortedByLikes() {
        Director director = new Director();
        director.setName("Режиссёр");
        directorRepository.addDirector(director);

        Film lessLiked = createFilm("lessLikes", LocalDate.of(2000, 1, 1));
        lessLiked.addDirector(director);
        Film moreLiked = createFilm("morelikes", LocalDate.of(2000, 1, 1));
        moreLiked.addDirector(director);
        lessLiked = filmRepository.create(lessLiked).get();
        moreLiked = filmRepository.create(moreLiked).get();

        User user1 = createUser("one@mail.ru", "login1");
        User user2 = createUser("two@mail.ru", "login2");
        user1 = userRepository.create(user1).get();
        user2 = userRepository.create(user2).get();

        filmRepository.addLike(moreLiked.getId(), user1.getId());
        filmRepository.addLike(moreLiked.getId(), user2.getId());
        filmRepository.addLike(lessLiked.getId(), user1.getId());

        assertThat(filmRepository.findFilmsByDirector(director.getId(), "likes"))
                .extracting(Film::getId)
                .containsExactly(moreLiked.getId(), lessLiked.getId());
        assertThat(filmRepository.findFilmsByDirector(director.getId(), "likes"))
                .extracting(Film::getName)
                .containsExactly(moreLiked.getName(), lessLiked.getName());
    }

    @Test
    void shouldThrowWhenDirectorNotFoundInSort() {
        assertThatThrownBy(() -> filmRepository.findFilmsByDirector(999L, "year"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldThrowWhenSortByUnknown() {
        Director director = new Director();
        director.setName("Режиссёр");
        directorRepository.addDirector(director);

        assertThatThrownBy(() -> filmRepository.findFilmsByDirector(director.getId(), "title"))
                .isInstanceOf(ValidationException.class);
    }

    private Film createFilm(String name, LocalDate releaseDate) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("description");
        film.setReleaseDate(releaseDate);
        film.setDuration(120L);
        Mpa mpa = new Mpa();
        mpa.setId(1L);
        film.setMpa(mpa);
        return film;
    }

    private User createUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("name");
        user.setBirthday(LocalDate.now());
        return user;
    }

}
