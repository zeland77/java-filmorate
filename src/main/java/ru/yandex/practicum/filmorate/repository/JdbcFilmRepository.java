package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.*;

@Repository("jdbcFilmRepository")
@RequiredArgsConstructor
@Slf4j
public class JdbcFilmRepository implements FilmRepository {
    private final NamedParameterJdbcOperations jdbc;
    private final FilmRowMapper filmRowMapper;

    public Collection<Film> findAll() {
        SqlParameterSource params = new MapSqlParameterSource();

        String sql = "SELECT films.*," +
                "mpa.name AS mpa_name, " +
                "films_genres.GENRE_ID, genres.NAME AS genre_name " +
                "FROM FILMS " +
                "LEFT JOIN films_genres ON films.id = films_genres.film_id " +
                "LEFT JOIN mpa ON films.MPA_ID = mpa.id " +
                "LEFT JOIN genres ON GENRE_ID = genres.id";

        return jdbc.query(sql, params, rs -> {
            Map<Long, Film> filmMap = new HashMap<>();

            while (rs.next()) {
                if (filmMap.containsKey(rs.getLong("id"))) {
                    Film film = filmMap.get(rs.getLong("id"));
                    Genre genre = new Genre();
                    genre.setId(rs.getLong("genre_id"));
                    genre.setName(rs.getString("genre_name"));
                    film.addGenre(genre);
                    continue;
                }
                Film newFilm = new Film();
                newFilm.setId(rs.getLong("id"));
                newFilm.setName(rs.getString("name"));
                newFilm.setDescription(rs.getString("description"));
                newFilm.setReleaseDate(rs.getDate("releaseDate").toLocalDate());
                newFilm.setDuration(rs.getLong("duration"));
                Mpa mpa = new Mpa();
                mpa.setId(rs.getLong("mpa_id"));
                mpa.setName(rs.getString("mpa_name"));
                newFilm.setMpa(mpa);
                Genre genre = new Genre();
                genre.setId(rs.getLong("genre_id"));
                genre.setName(rs.getString("genre_name"));
                if (genre.getId() != 0) newFilm.addGenre(genre);
                filmMap.put(newFilm.getId(), newFilm);
            }
            return filmMap.values().stream().toList();
        });
}

    public Optional<Film> create(Film film) {
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(film);
            jdbc.update("INSERT INTO films(name, description, releaseDate, duration, mpa_id)" +
                            " VALUES (:name, :description, :releaseDate, :duration, :mpa.id)",
                    namedParameters,
                    keyHolder,
                    new String[]{"id"}
            );
            Long filmId = keyHolder.getKey().longValue();
            String sql = "MERGE INTO films_genres(film_id, genre_id) KEY (film_id, genre_id) VALUES (:filmId, :genreId)";
            SqlParameterSource[] batchArgs = film.getGenres().stream()
                    .map(g -> new MapSqlParameterSource().addValue("filmId", filmId)
                            .addValue("genreId", g.getId()))
                    .toArray(MapSqlParameterSource[]::new);
            jdbc.batchUpdate(sql, batchArgs);
            return getFilm(filmId);
        } catch (DuplicateKeyException e) {
            log.error("Ошибка при создании фильма. Такой фильм уже существует.");
            return Optional.empty();
        } catch (DataIntegrityViolationException e) {
            log.error("Ошибка вставки жанров для фильма: нарушена целостность данных.");
            throw new NotFoundException("Ошибка вставки рейтинга/жанров для фильма: нарушена целостность данных.");
        }
    }

    public Film delete(Film film) {
        jdbc.update("DELETE FROM films_genres WHERE film_id = :id",
                Map.of("id", film.getId())
        );
        jdbc.update("DELETE FROM films WHERE id = :id",
                Map.of("id", film.getId())
        );
        return film;
    }

    public Optional<Film> update(Film film) {
        SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(film);
        jdbc.update("UPDATE films SET name = :name, description = :description, releaseDate = :releaseDate," +
                        " duration = :duration WHERE id = :id",
                namedParameters
        );
        return getFilm(film.getId());
    }

    public Optional<Film> getFilm(Long id) {
        SqlParameterSource params = new MapSqlParameterSource("id", id);

        String sql = "SELECT films.*," +
                "mpa.name AS mpa_name, " +
                "films_genres.GENRE_ID, genres.NAME AS genre_name " +
                "FROM FILMS " +
                "LEFT JOIN films_genres ON films.id = films_genres.film_id " +
                "LEFT JOIN mpa ON films.MPA_ID = mpa.id " +
                "LEFT JOIN genres ON GENRE_ID = genres.id " +
                "WHERE films.id = :id";

        return jdbc.query(sql, params, rs -> {
            Map<Long, Film> filmMap = new HashMap<>();

            while (rs.next()) {
                if (filmMap.containsKey(id)) {
                    Film film = filmMap.get(id);
                    Genre genre = new Genre();
                    genre.setId(rs.getLong("genre_id"));
                    genre.setName(rs.getString("genre_name"));
                    film.addGenre(genre);
                    continue;
                }
                Film newFilm = new Film();
                newFilm.setId(rs.getLong("id"));
                newFilm.setName(rs.getString("name"));
                newFilm.setDescription(rs.getString("description"));
                newFilm.setReleaseDate(rs.getDate("releaseDate").toLocalDate());
                newFilm.setDuration(rs.getLong("duration"));
                Mpa mpa = new Mpa();
                mpa.setId(rs.getLong("mpa_id"));
                mpa.setName(rs.getString("mpa_name"));
                newFilm.setMpa(mpa);
                Genre genre = new Genre();
                genre.setId(rs.getLong("genre_id"));
                genre.setName(rs.getString("genre_name"));
                if (genre.getId() != 0) newFilm.addGenre(genre);
                filmMap.put(newFilm.getId(), newFilm);
            }
            return filmMap.values().stream().findFirst();
        });
    }

    public void addLike(Long filmId, Long userId) {
        jdbc.update("MERGE INTO likes (film_id, user_id) KEY (film_id, user_id)" +
                        " VALUES (:filmId, :userId)",
                Map.of("filmId", filmId,
                        "userId", userId));
    }

    public void removeLike(Long filmId, Long userId) {
        jdbc.update("DELETE FROM likes WHERE user_id = :userId AND film_id = :filmId",
                Map.of("userId", userId,
                        "filmId", filmId));
    }

    public Collection<Film> mostPopular(Integer count) {
        SqlParameterSource params = new MapSqlParameterSource();
        String sql = "SELECT films.*, mpa.name AS mpa_name, films_genres.GENRE_ID, genres.NAME AS genre_name FROM FILMS " +
                "LEFT JOIN films_genres ON films.id = films_genres.film_id " +
                "LEFT JOIN mpa ON films.MPA_ID = mpa.id " +
                "LEFT JOIN genres ON GENRE_ID = genres.id " +
                "WHERE films.id IN (SELECT film_id FROM likes GROUP BY film_id ORDER BY COUNT(user_id) DESC LIMIT 10)";
        Map<Long, Film> filmPopular = jdbc.query(sql, params, rs -> {
            Map<Long, Film> filmMap = new HashMap<>();

            while (rs.next()) {
                if (filmMap.containsKey(rs.getLong("id"))) {
                    Film film = filmMap.get(rs.getLong("id"));
                    Genre genre = new Genre();
                    genre.setId(rs.getLong("genre_id"));
                    genre.setName(rs.getString("genre_name"));
                    film.addGenre(genre);
                    continue;
                }
                Film newFilm = new Film();
                newFilm.setId(rs.getLong("id"));
                newFilm.setName(rs.getString("name"));
                newFilm.setDescription(rs.getString("description"));
                newFilm.setReleaseDate(rs.getDate("releaseDate").toLocalDate());
                newFilm.setDuration(rs.getLong("duration"));
                Mpa mpa = new Mpa();
                mpa.setId(rs.getLong("mpa_id"));
                mpa.setName(rs.getString("mpa_name"));
                newFilm.setMpa(mpa);
                Genre genre = new Genre();
                genre.setId(rs.getLong("genre_id"));
                genre.setName(rs.getString("genre_name"));
                if (genre.getId() != 0) newFilm.addGenre(genre);
                filmMap.put(newFilm.getId(), newFilm);
            }
            return filmMap;
        });

        String sql2 = "SELECT film_id FROM likes GROUP BY film_id ORDER BY COUNT(user_id) DESC LIMIT 10";

        List<Long> listIdPopular = jdbc.queryForList(sql2, params, Long.class);
        List<Film> filmPopularSorted = new ArrayList<>();
        for (Long id : listIdPopular) {
            filmPopularSorted.add(filmPopular.get(id));
        }
        return filmPopularSorted;
    }

}
