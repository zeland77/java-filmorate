package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

@Repository("jdbcGenreRepository")
@RequiredArgsConstructor
@Slf4j
public class JdbcGenreRepository implements GenreRepository {
    private final NamedParameterJdbcOperations jdbc;
    private final GenreRowMapper genreRowMapper;

    public Collection<Genre> findAll() {
        List<Genre> genres = jdbc.query("SELECT * FROM genres", genreRowMapper);
        return genres;
    }

    public Optional<Genre> getGenre(Long id) {
        List<Genre> results = jdbc.query(
                "SELECT * FROM genres WHERE id = :id",
                Map.of("id", id),
                genreRowMapper);
        return results.stream().findFirst();
    }

}
