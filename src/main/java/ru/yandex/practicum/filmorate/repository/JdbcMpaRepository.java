package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository("jdbcMpaRepository")
@RequiredArgsConstructor
@Slf4j
public class JdbcMpaRepository implements MpaRepository {
    private final NamedParameterJdbcOperations jdbc;
    private final MpaRowMapper mpaRowMapper;

    public Collection<Mpa> findAll() {
        List<Mpa> mpa = jdbc.query("SELECT * FROM mpa", mpaRowMapper);
        return mpa;
    }

    public Optional<Mpa> getMpa(Long id) {
        List<Mpa> results = jdbc.query(
                "SELECT * FROM mpa WHERE id = :id",
                Map.of("id", id),
                mpaRowMapper);
        return results.stream().findFirst();
    }

}
