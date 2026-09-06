package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    Collection<Film> findAll();

    Film create(Film film) throws ValidationException;

    Film delete(Film film) throws ValidationException;

    Film update(Film newFilm) throws ValidationException;

    Film getFilm(Long id);
}
