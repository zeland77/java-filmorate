package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

public interface MpaRepository {
    Collection<Mpa> findAll();

    Optional<Mpa> getMpa(Long id);
}
