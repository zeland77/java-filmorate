package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {
    public Collection<Film> findAll();
    public Film create(Film film) throws ValidationException;
    public Film delete(Film film) throws ValidationException;
    public Film update(Film newFilm) throws ValidationException;
    public Film getFilm(Long id);
}
