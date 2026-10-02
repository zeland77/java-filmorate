package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.JdbcFilmRepository;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@Import({JdbcFilmRepository.class, FilmRowMapper.class})
@DisplayName("JdbcUserRepository")
public class JdbcFilmRepositoryTest {
    public static final long TEST_FILM_ID = 1L;
    @Autowired
    JdbcFilmRepository filmRepository;

    static Film getTestFilm() {
        Film film = new Film();
        film.setId(TEST_FILM_ID);
        film.setName("film");
        film.setDescription("dfghdjyuyuiukjnvbnvccxfgder");
        film.setReleaseDate(LocalDate.of(2000, 3, 20));
        film.setDuration(100L);
        Mpa mpa = new Mpa();
        mpa.setId(2L);
        mpa.setName("PG");
        film.setMpa(mpa);
        Genre genre = new Genre();
        genre.setId(3L);
        genre.setName("Мультфильм");
        film.addGenre(genre);
        return film;
    }

    @Test
    @DisplayName("Фильм по ид")
    public void shouldReturnFilmWhenFindById() {
        Film testFilm = getTestFilm();
        Optional<Film> filmOptional = filmRepository.getFilm(testFilm.getId());

        assertThat(filmOptional)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(testFilm);
    }

    @Test
    @DisplayName("Обновление фильма")
    public void shouldUpdateFilm() {
        Film testFilm = getTestFilm();
        testFilm.setName("fgtyu");
        testFilm.setDuration(30L);
        Optional<Film> filmOptional = filmRepository.update(testFilm);

        assertThat(filmOptional)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(testFilm);
    }

    @Test
    @DisplayName("Удаление фильма")
    public void shouldDeleteFilm() {
        Film testFilm = getTestFilm();
        Film film = filmRepository.delete(testFilm);

        assertThat(film)
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(testFilm);
    }

}
