package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) throws ValidationException {
        validateFilmFields(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Добавлен фильм {}", film);
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) throws ValidationException {
        if (newFilm.getId() == null) {
            log.error("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }
        if (!films.containsKey(newFilm.getId())) {
            log.error("Фильм с id = {} не найден", newFilm.getId());
            throw new ValidationException("Фильм с id = " + newFilm.getId() + " не найден");
        }
        validateFilmFields(newFilm);
        Film oldFilm = films.get(newFilm.getId());
        oldFilm.setName(newFilm.getName());
        oldFilm.setDescription(newFilm.getDescription());
        oldFilm.setReleaseDate(newFilm.getReleaseDate());
        oldFilm.setDuration(newFilm.getDuration());
        log.info("Обновлены данные фильма {}", oldFilm);
        return oldFilm;
    }

    private void validateFilmFields(Film film) throws ValidationException {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Название фильма не может быть пустым");
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription().length() > Film.MAX_LENGTH_DESCRIPTION) {
            log.error("Длина описания больше {} символов", Film.MAX_LENGTH_DESCRIPTION);
            throw new ValidationException("Максимальная длина описания " + Film.MAX_LENGTH_DESCRIPTION + " символов");
        }
        if (film.getReleaseDate().isBefore(Film.MIN_FILM_RELEASE_DATE)) {
            log.error("Дата релиза фильма  раньше {}",
                    Film.MIN_FILM_RELEASE_DATE.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            throw new ValidationException("Дата релиза фильма не может быть раньше "
                    + Film.MIN_FILM_RELEASE_DATE.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        }
        if (film.getDuration() < 0) {
            log.error("Некорректная продолжительность фильма {} минут", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

}
