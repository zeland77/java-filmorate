package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.*;
import java.util.stream.Collectors;

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
                "films_genres.GENRE_ID, genres.NAME AS genre_name, fd.director_id, d.name AS d_name " +
                "FROM FILMS " +
                "LEFT JOIN films_genres ON films.id = films_genres.film_id " +
                "LEFT JOIN mpa ON films.MPA_ID = mpa.id " +
                "LEFT JOIN genres ON GENRE_ID = genres.id " +
                "LEFT JOIN film_directors fd ON films.id = fd.film_id " +
                "LEFT JOIN directors d ON d.id = fd.director_id ";

        return jdbc.query(sql, params, rs -> {
            Map<Long, Film> filmMap = new HashMap<>();

            while (rs.next()) {
                if (filmMap.containsKey(rs.getLong("id"))) {
                    Film film = filmMap.get(rs.getLong("id"));
                    Genre genre = new Genre();
                    genre.setId(rs.getLong("genre_id"));
                    genre.setName(rs.getString("genre_name"));

                    Director director = new Director();
                    director.setId(rs.getLong("director_id"));
                    director.setName(rs.getString("d_name"));

                    if (genre.getId() != 0)
                        film.addGenre(genre);

                    if (director.getId() != 0)
                        film.addDirector(director);

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

                Director director = new Director();
                director.setId(rs.getLong("director_id"));
                director.setName(rs.getString("d_name"));
                if (director.getId() != 0) newFilm.addDirector(director);

                filmMap.put(newFilm.getId(), newFilm);
            }
            return filmMap.values().stream().toList();
        });
    }

    public Optional<Film> create(Film film) {
        Set<Long> dirIdsSet = film.getDirectors().stream().map(Director::getId).collect(Collectors.toSet());
        Set<Long> genreIdsSet = film.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
        Set<Long> mpaIdSet = Set.of(film.getMpa().getId());

        checkIdsExist(dirIdsSet, "directors", "Режиссеры не найдены: ");
        checkIdsExist(genreIdsSet, "genres", "Жанры не найдены: ");
        checkIdsExist(mpaIdSet, "mpa", "Рейтинг не найден: ");

        KeyHolder keyHolder = new GeneratedKeyHolder();
        SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(film);
        jdbc.update("INSERT INTO films(name, description, releaseDate, duration, mpa_id)" +
                        " VALUES (:name, :description, :releaseDate, :duration, :mpa.id)",
                namedParameters,
                keyHolder,
                new String[]{"id"}
        );
        Long filmId = keyHolder.getKey().longValue();

        insertLinks(filmId, dirIdsSet, "film_directors(film_id, director_id)");
        insertLinks(filmId, genreIdsSet, "films_genres(film_id, genre_id)");

        return getFilm(filmId);
    }

    private void insertLinks(Long filmId, Set<Long> linkedIdsSet, String tableWithArgs) {
        if (linkedIdsSet.isEmpty()) return;

        List<Long> insertedIds = linkedIdsSet.stream()
                .toList();

        String placeholders = insertedIds.stream()
                .map(id -> "(?, ?)")
                .collect(Collectors.joining(", "));
        String query = ("INSERT INTO %s " +
                "VALUES " + placeholders).formatted(tableWithArgs);
        Object[] args = new Object[insertedIds.size() * 2];
        for (int i = 0; i < insertedIds.size(); i++) {
            args[i * 2] = filmId;
            args[i * 2 + 1] = insertedIds.get(i);
        }
        jdbc.getJdbcOperations().update(query, args);

    }

    private void checkIdsExist(Set<Long> ids, String table, String message) {
        if (ids.isEmpty()) return;

        List<Long> checkedIds = ids.stream()
                .toList();

        String placeholders = checkedIds.stream()
                .map(id -> "(?)")
                .collect(Collectors.joining(", "));
        String query = """
                SELECT requested.id
                FROM (VALUES %s) AS requested(id)
                WHERE NOT EXISTS (
                    SELECT 1 FROM %s t WHERE t.id = requested.id
                )
                """.formatted(placeholders, table);

        List<Long> nonexistentIds = jdbc.getJdbcOperations().query(query, (rs, rowNum) ->
                rs.getLong("id"), checkedIds.toArray());
        if (!nonexistentIds.isEmpty())
            throw new NotFoundException(message + nonexistentIds);

    }

    public Film delete(Film film) {
        jdbc.update("DELETE FROM films WHERE id = :id",
                Map.of("id", film.getId())
        );
        return film;
    }

    public Optional<Film> update(Film film) {
        Set<Long> dirIdsSet = film.getDirectors().stream().map(Director::getId).collect(Collectors.toSet());
        Set<Long> genreIdsSet = film.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
        Set<Long> mpaIdSet = Set.of(film.getMpa().getId());

        checkIdsExist(dirIdsSet, "directors", "Режиссеры не найдены: ");
        checkIdsExist(genreIdsSet, "genres", "Жанры не найдены: ");
        checkIdsExist(mpaIdSet, "mpa", "Рейтинг не найден: ");

        SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(film);
        jdbc.update("UPDATE films SET name = :name, description = :description, releaseDate = :releaseDate," +
                        " duration = :duration, mpa_id = :mpa.id WHERE id = :id",
                namedParameters
        );

        jdbc.getJdbcOperations().update("DELETE FROM film_directors WHERE film_id = ?", film.getId());
        insertLinks(film.getId(), dirIdsSet, "film_directors(film_id, director_id)");

        jdbc.getJdbcOperations().update("DELETE FROM films_genres WHERE film_id = ?", film.getId());
        insertLinks(film.getId(), genreIdsSet, "films_genres(film_id, genre_id)");

        return getFilm(film.getId());
    }

    public Optional<Film> getFilm(Long id) {
        SqlParameterSource params = new MapSqlParameterSource("id", id);

        String sql = "SELECT films.*," +
                "mpa.name AS mpa_name, " +
                "films_genres.GENRE_ID, genres.NAME AS genre_name, fd.director_id, d.name AS d_name " +
                "FROM FILMS " +
                "LEFT JOIN films_genres ON films.id = films_genres.film_id " +
                "LEFT JOIN mpa ON films.MPA_ID = mpa.id " +
                "LEFT JOIN genres ON GENRE_ID = genres.id " +
                "LEFT JOIN film_directors fd ON films.id = fd.film_id " +
                "LEFT JOIN directors d ON d.id = fd.director_id " +
                "WHERE films.id = :id";

        return jdbc.query(sql, params, rs -> {
            Map<Long, Film> filmMap = new HashMap<>();

            while (rs.next()) {
                if (filmMap.containsKey(id)) {
                    Film film = filmMap.get(id);
                    Genre genre = new Genre();
                    genre.setId(rs.getLong("genre_id"));
                    genre.setName(rs.getString("genre_name"));

                    Director director = new Director();
                    director.setId(rs.getLong("director_id"));
                    director.setName(rs.getString("d_name"));

                    if (genre.getId() != 0)
                        film.addGenre(genre);

                    if (director.getId() != 0)
                        film.addDirector(director);
                    continue;
                }
                Film newFilm = new Film();
                newFilm.setDirectors(new TreeSet<>());

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

                Director director = new Director();
                director.setId(rs.getLong("director_id"));
                director.setName(rs.getString("d_name"));
                if (director.getId() != 0) newFilm.addDirector(director);

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
        List<Film> sortedFilms = jdbc.getJdbcOperations().query("""
                SELECT f.id, f.name, f.description, f.releaseDate, f.duration,
                f.mpa_id, m.name AS mpa_name
                FROM films f
                JOIN mpa m ON m.id = f.mpa_id
                LEFT JOIN (
                    SELECT film_id, COUNT(*) AS likes_count
                    FROM likes
                    GROUP BY film_id
                ) likes_by_film ON likes_by_film.film_id = f.id
                ORDER BY likes_by_film.likes_count DESC
                LIMIT ?
                """, filmRowMapper, count);

        Map<Long, Film> filmMap = sortedFilms.stream().collect(Collectors.toMap(Film::getId, f -> f));

        String ids = sortedFilms.stream()
                .map(f -> f.getId().toString())
                .collect(Collectors.joining(","));
        jdbc.getJdbcOperations().query(

                "SELECT fg.film_id, g.id AS genre_id, g.name AS genre_name " +
                        "FROM films_genres fg JOIN genres g ON g.id = fg.genre_id " +
                        "WHERE fg.film_id IN (" + ids + ")",
                (rs, rowNum) -> {
                    Film film = filmMap.get(rs.getLong("film_id"));
                    if (film != null) {
                        Genre genre = new Genre();
                        genre.setId(rs.getLong("genre_id"));
                        genre.setName(rs.getString("genre_name"));
                        film.addGenre(genre);
                    }
                    return null;
                });

        jdbc.getJdbcOperations().query(
                "SELECT fd.film_id, d.id AS director_id, d.name AS director_name " +
                        "FROM film_directors fd JOIN directors d ON d.id = fd.director_id " +
                        "WHERE fd.film_id IN (" + ids + ")",
                (rs, rowNum) -> {
                    Film film = filmMap.get(rs.getLong("film_id"));
                    if (film != null) {
                        Director director = new Director();
                        director.setId(rs.getLong("director_id"));
                        director.setName(rs.getString("director_name"));
                        film.addDirector(director);
                    }
                    return null;
                });

        return sortedFilms;
    }

    public Collection<Film> findFilmsByDirector(Long directorId, String sortBy) {
        checkIdsExist(Set.of(directorId), "directors", "Режиссеры не найдены: ");
        String rule = switch (sortBy) {
            case "likes" -> "likes_by_film.likes_count DESC";
            case "year" -> "f.releaseDate";
            default -> throw new ValidationException("неизвестный запрос на сортировку");
        };

        List<Film> sortedFilms = jdbc.getJdbcOperations().query("""
                SELECT f.id, f.name, f.description, f.releaseDate, f.duration,
                f.mpa_id, m.name AS mpa_name
                FROM films f
                JOIN mpa m ON m.id = f.mpa_id
                LEFT JOIN (
                    SELECT film_id, COUNT(*) AS likes_count
                    FROM likes
                    GROUP BY film_id
                ) likes_by_film ON likes_by_film.film_id = f.id
                WHERE f.id IN (SELECT film_id FROM film_directors WHERE director_id = ?)
                ORDER BY %s
                """.formatted(rule), filmRowMapper, directorId);

        Map<Long, Film> filmMap = new HashMap<>();
        for (Film film : sortedFilms) {
            film.setDirectors(new TreeSet<>());
            filmMap.put(film.getId(), film);
        }

        jdbc.getJdbcOperations().query("""
                        SELECT fd.film_id, fg.genre_id, g.name AS genre_name, fd.director_id, d.name AS director_name
                        FROM film_directors fd
                        LEFT JOIN films_genres fg ON fg.film_id = fd.film_id
                        LEFT JOIN genres g ON fg.genre_id = g.id
                        LEFT JOIN directors d ON d.id = fd.director_id
                        WHERE d.id = ?
                        """,
                (rs, rowNum) -> {
                    Film film = filmMap.get(rs.getLong("film_id"));
                    if (film != null) {
                        Genre genre = new Genre();
                        genre.setId(rs.getLong("genre_id"));
                        genre.setName(rs.getString("genre_name"));

                        Director director = new Director();
                        director.setId(rs.getLong("director_id"));
                        director.setName(rs.getString("director_name"));

                        if (genre.getId() != 0)
                            film.addGenre(genre);

                        if (director.getId() != 0)
                            film.addDirector(director);

                        return film;
                    }
                    return null;
                },
                directorId
        );
        return sortedFilms;
    }

    public Collection<Film> mostPopular(Integer count, Long genreId, Integer year) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", count)
                .addValue("genreId", genreId)
                .addValue("year", year);

        String sql = """
           SELECT films.*, mpa.name AS mpa_name
           FROM FILMS
           LEFT JOIN mpa ON films.MPA_ID = mpa.id
           LEFT JOIN likes ON films.id = likes.film_id
           WHERE (:year IS NULL OR EXTRACT(YEAR FROM films.releaseDate) = :year)
             AND (:genreId IS NULL OR films.id IN (SELECT film_id FROM films_genres WHERE genre_id = :genreId))
           GROUP BY films.id, mpa.name
           ORDER BY COUNT(likes.user_id) DESC
           %s
           """.formatted(count != null ? "LIMIT :limit" : "");

        List<Film> sortedFilms = jdbc.query(sql, params, filmRowMapper);

        Map<Long, Film> filmMap = sortedFilms.stream().collect(Collectors.toMap(Film::getId, f -> f));

        String ids = sortedFilms.stream()
                .map(f -> f.getId().toString())
                .collect(Collectors.joining(","));

        jdbc.getJdbcOperations().query(
                "SELECT fg.film_id, g.id AS genre_id, g.name AS genre_name " +
                        "FROM films_genres fg JOIN genres g ON g.id = fg.genre_id " +
                        "WHERE fg.film_id IN (" + ids + ")",
                (rs, rowNum) -> {
                    Film film = filmMap.get(rs.getLong("film_id"));
                    if (film != null) {
                        Genre genre = new Genre();
                        genre.setId(rs.getLong("genre_id"));
                        genre.setName(rs.getString("genre_name"));
                        film.addGenre(genre);
                    }
                    return null;
                });

        jdbc.getJdbcOperations().query(
                "SELECT fd.film_id, d.id AS director_id, d.name AS director_name " +
                        "FROM film_directors fd JOIN directors d ON d.id = fd.director_id " +
                        "WHERE fd.film_id IN (" + ids + ")",
                (rs, rowNum) -> {
                    Film film = filmMap.get(rs.getLong("film_id"));
                    if (film != null) {
                        Director director = new Director();
                        director.setId(rs.getLong("director_id"));
                        director.setName(rs.getString("director_name"));
                        film.addDirector(director);
                    }
                    return null;
                });

        return sortedFilms;
    }

}
