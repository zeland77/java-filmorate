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

        try {
            checkDirectorIds(dirIdsSet);
            KeyHolder keyHolder = new GeneratedKeyHolder();
            SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(film);
            jdbc.update("INSERT INTO films(name, description, releaseDate, duration, mpa_id)" +
                            " VALUES (:name, :description, :releaseDate, :duration, :mpa.id)",
                    namedParameters,
                    keyHolder,
                    new String[]{"id"}
            );
            Long filmId = keyHolder.getKey().longValue();

            insertIntoFilmDirectors(filmId, dirIdsSet);

            String sql = "MERGE INTO films_genres(film_id, genre_id) KEY (film_id, genre_id) VALUES (:filmId, :genreId)";
            log.info("перед запрос еще ок");
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

    public void checkDirectorIds(Set<Long> ids) {
        if (!ids.isEmpty()) {
            List<Long> directorIds = ids.stream()
                    .distinct()
                    .toList();

            String placeholders = directorIds.stream()
                    .map(id -> "(?)")
                    .collect(Collectors.joining(", "));
            String query = """
                    SELECT requested.id
                    FROM (VALUES %s) AS requested(id)
                    WHERE NOT EXISTS (
                        SELECT 1 FROM directors d WHERE d.id = requested.id
                    )
                    """.formatted(placeholders);

            List<Long> nonexistentIds = jdbc.getJdbcOperations().query(query, (rs, rowNum) ->
                    rs.getLong("id"), directorIds.toArray());
            if (!nonexistentIds.isEmpty())
                throw new NotFoundException("Режиссеры не найдены: " + ids);
        }
    }

    private void insertIntoFilmDirectors(Long filmId, Set<Long> dirIdsSet) {
        if (!dirIdsSet.isEmpty()) {
            List<Long> directorIds = dirIdsSet.stream()
                    .distinct()
                    .toList();

            String placeholders = directorIds.stream()
                    .map(id -> "(?, ?)")
                    .collect(Collectors.joining(", "));
            String query = "INSERT INTO film_directors(film_id, director_id) " +
                    "VALUES " + placeholders;
            Object[] args = new Object[directorIds.size() * 2];
            for (int i = 0; i < directorIds.size(); i++) {
                args[i * 2] = filmId;
                args[i * 2 + 1] = directorIds.get(i);
            }
            jdbc.getJdbcOperations().update(query, args);
        }
    }

    public Film delete(Film film) {
        jdbc.update("DELETE FROM films_genres WHERE film_id = :id",
                Map.of("id", film.getId())
        );
        jdbc.update("DELETE FROM likes WHERE film_id = :id",
                Map.of("id", film.getId())
        );
        jdbc.update("DELETE FROM films WHERE id = :id",
                Map.of("id", film.getId())
        );
        return film;
    }

    public Optional<Film> update(Film film) {
        Set<Long> dirIdsSet = film.getDirectors().stream().map(Director::getId).collect(Collectors.toSet());
        checkDirectorIds(dirIdsSet);

        SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(film);
        jdbc.update("UPDATE films SET name = :name, description = :description, releaseDate = :releaseDate," +
                        " duration = :duration, mpa_id = :mpa.id WHERE id = :id",
                namedParameters
        );

        jdbc.getJdbcOperations().update("DELETE FROM film_directors WHERE film_id = ?", film.getId());
        insertIntoFilmDirectors(film.getId(), dirIdsSet);

        jdbc.getJdbcOperations().update("DELETE FROM films_genres WHERE film_id = ?", film.getId());
        if (!film.getGenres().isEmpty()) {
            List<Long> genreIds = film.getGenres().stream()
                    .map(Genre::getId)
                    .distinct()
                    .toList();

            String placeholders = genreIds.stream()
                    .map(id -> "(?, ?)")
                    .collect(Collectors.joining(", "));
            String query =
                    "INSERT INTO films_genres(film_id, genre_id) VALUES" + placeholders;
            Object[] args = new Object[genreIds.size() * 2];
            for (int i = 0; i < genreIds.size(); i++) {
                args[i * 2] = film.getId();
                args[i * 2 + 1] = genreIds.get(i);
            }
            jdbc.getJdbcOperations().update(query, args);
        }

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
        SqlParameterSource params = new MapSqlParameterSource("limit", count);
        String sql = "SELECT films.*, mpa.name AS mpa_name, films_genres.GENRE_ID, genres.NAME AS genre_name FROM FILMS " +
                "LEFT JOIN films_genres ON films.id = films_genres.film_id " +
                "LEFT JOIN mpa ON films.MPA_ID = mpa.id " +
                "LEFT JOIN genres ON GENRE_ID = genres.id " +
                "WHERE films.id IN (SELECT film_id FROM likes GROUP BY film_id ORDER BY COUNT(user_id) DESC LIMIT :limit)";
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

        String sql2 = "SELECT film_id FROM likes GROUP BY film_id ORDER BY COUNT(user_id) DESC LIMIT :limit";

        List<Long> listIdPopular = jdbc.queryForList(sql2, params, Long.class);
        List<Film> filmPopularSorted = new ArrayList<>();
        for (Long id : listIdPopular) {
            filmPopularSorted.add(filmPopular.get(id));
        }
        return filmPopularSorted;
    }

    public Collection<Film> findFilmsByDirector(Long directorId, String sortBy) {
        checkDirectorIds(Set.of(directorId));
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
}
