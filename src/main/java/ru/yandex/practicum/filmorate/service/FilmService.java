package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.Comparator;

@Service
@RequestMapping("/films")
@Slf4j
public class FilmService {
    FilmStorage filmStorage;
    UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Collection<Film> mostPopular(Integer count) {
        if (count <= 0) {
            throw new ValidationException("Количество популярных фильмов должно быть положительным");
        }
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt(Film::countLikes).reversed())
                .limit(count)
                .toList();
    }

    public Film addLike(Long filmId, Long userId) {
        if (userStorage.findAll().stream().filter(user -> user.getId() == userId).findFirst().isEmpty()) {
            log.error("Пользователь с id = {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        if (filmStorage.findAll().stream().filter(film -> film.getId() == filmId).findFirst().isEmpty()) {
            log.error("Фильм с id = {} не найден", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        Film film = filmStorage.getFilm(filmId);
        film.setLike(userId);
        return film;
    }

    public Film deleteLike(Long filmId, Long userId) {
        if (userStorage.findAll().stream().filter(user -> user.getId() == userId).findFirst().isEmpty()) {
            log.error("Пользователь с id = {} не найден", userId);
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
        if (filmStorage.findAll().stream().filter(film -> film.getId() == filmId).findFirst().isEmpty()) {
            log.error("Фильм с id = {} не найден", filmId);
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
        Film film = filmStorage.getFilm(filmId);
        film.removeLike(userId);
        return film;
    }
}