package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.DirectorRowMapper;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.*;

@RequiredArgsConstructor
@Repository
public class JdbcDirectorRepository implements DirectorRepository {
    private static final String FIND_DIRECTOR_BY_ID_QUERY = "SELECT * FROM directors WHERE id = ?";
    private static final String FIND_COUNT_DIRECTORS_BY_NAME = "SELECT COUNT(*) FROM directors " +
            "WHERE LOWER(name) = LOWER(?)";
    private static final String FIND_COUNT_OTHER_DIRECTORS_BY_NAME = "SELECT COUNT(*) FROM directors " +
            "WHERE LOWER(name) = LOWER(?) AND id <> ?";
    private static final String FIND_ALL_DIRECTORS_QUERY = "SELECT * FROM directors";
    private static final String UPDATE_DIRECTOR_QUERY = "UPDATE directors SET name = ? WHERE id = ?";
    private static final String DELETE_DIRECTOR_BY_ID = "DELETE FROM directors WHERE id = ?";

    private final JdbcTemplate jdbc;
    private final DirectorRowMapper directorRowMapper;

    @Override
    public Director addDirector(Director newDirector) {
        Long count = jdbc.queryForObject(FIND_COUNT_DIRECTORS_BY_NAME, Long.class, newDirector.getName());
        if (count > 0)
            throw new ValidationException("уже добавлен режиссер с именем " + newDirector.getName());

        try {
            SimpleJdbcInsert insertDir = new SimpleJdbcInsert(jdbc)
                    .withTableName("directors")
                    .usingGeneratedKeyColumns("id")
                    .usingColumns("name"); // какие колонки вставлять

            Map<String, Object> params = new HashMap<>(Map.of("name", newDirector.getName()));
            Long id = insertDir.executeAndReturnKey(params).longValue();
            newDirector.setId(id);
        } catch (DataAccessException e) {
            throw new InternalServerException("не удалось сохранить данные");
        }
        return newDirector;
    }

    @Override
    public Director findDirById(Long dirId) {
        try {
            Director result = jdbc.queryForObject(FIND_DIRECTOR_BY_ID_QUERY, directorRowMapper, dirId);
            return result;
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("не найден режиссер с id " + dirId);
        }
    }

    @Override
    public List<Director> findAllDirectors() {
        return jdbc.query(FIND_ALL_DIRECTORS_QUERY, directorRowMapper).stream()
                .sorted(Comparator.comparingLong(Director::getId)).toList();
    }

    @Override
    public Director updateDirector(Director director) {
        findDirById(director.getId());
        Long count = jdbc.queryForObject(FIND_COUNT_OTHER_DIRECTORS_BY_NAME,
                Long.class, director.getName(), director.getId());
        if (count > 0)
            throw new ValidationException("уже добавлен режиссер с именем " + director.getName());

        int rowsUpdated = jdbc.update(UPDATE_DIRECTOR_QUERY, director.getName(), director.getId());
        if (rowsUpdated == 0)
            throw new InternalServerException("Не удалось обновить данные");
        return director;
    }

    @Override
    public void deleteDirector(Long dirId) {
        jdbc.update(DELETE_DIRECTOR_BY_ID, dirId);
    }
}
