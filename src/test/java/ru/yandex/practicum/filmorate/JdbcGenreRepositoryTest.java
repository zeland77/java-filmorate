package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.JdbcGenreRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@Import({JdbcGenreRepository.class, GenreRowMapper.class})
@DisplayName("JdbcGenreRepository")
public class JdbcGenreRepositoryTest {
    @Autowired
    JdbcGenreRepository genreRepository;

    @Test
    @DisplayName("Жанр по ид")
    public void shouldReturnGenreWhenFindById() {
        Genre testGenre = new Genre();
        testGenre.setId(2L);
        testGenre.setName("Драма");
        Optional<Genre> genreOptional = genreRepository.getGenre(testGenre.getId());

        assertThat(genreOptional)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(testGenre);
    }

    @Test
    @DisplayName("Все жанры")
    public void shouldReturnAllGenres() {
        Collection<Genre> allGenres = new ArrayList<>();
        Genre testGenre1 = new Genre();
        testGenre1.setId(1L);
        testGenre1.setName("Комедия");
        allGenres.add(testGenre1);
        Genre testGenre2 = new Genre();
        testGenre2.setId(2L);
        testGenre2.setName("Драма");
        allGenres.add(testGenre2);
        Genre testGenre3 = new Genre();
        testGenre3.setId(3L);
        testGenre3.setName("Мультфильм");
        allGenres.add(testGenre3);
        Genre testGenre4 = new Genre();
        testGenre4.setId(4L);
        testGenre4.setName("Триллер");
        allGenres.add(testGenre4);
        Genre testGenre5 = new Genre();
        testGenre5.setId(5L);
        testGenre5.setName("Документальный");
        allGenres.add(testGenre5);
        Genre testGenre6 = new Genre();
        testGenre6.setId(6L);
        testGenre6.setName("Боевик");
        allGenres.add(testGenre6);

        Collection<Genre> genre = genreRepository.findAll();

        assertThat(genre)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringExpectedNullFields()
                .isEqualTo(allGenres);
    }

}
