package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.time.format.DateTimeFormatter;
import java.util.Collection;

@Service
@Slf4j
public class FilmService {
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final EventService eventService;

    public FilmService(FilmRepository filmRepository, UserRepository userRepository, EventService eventService) {
        this.filmRepository = filmRepository;
        this.userRepository = userRepository;
        this.eventService = eventService;
    }

    public Collection<Film> findAll() {
        return filmRepository.findAll();
    }

    public Film getFilm(Long id) throws ValidationException {
        return filmRepository.getFilm(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден"));
    }

    public Film create(Film film) throws ValidationException {
        validateFilmFields(film);
        return filmRepository.create(film)
                .orElseThrow(() -> new ValidationException("Фильм с таким именем = " + film.getName() + " уже зарегистрирован"));
    }

    public Film update(Film film) throws ValidationException {
        validateFilmFields(film);
        getFilm(film.getId());
        return filmRepository.update(film)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + film.getId() + " не найден"));
    }

    @Transactional
    public Film delete(Long filmId) {
        Film film = getFilm(filmId);
        return filmRepository.delete(film);
    }

    public Collection<Film> mostPopular(Integer count) throws ValidationException {
        if (count <= 0) {
            throw new ValidationException("Количество популярных фильмов должно быть положительным");
        }
        return filmRepository.mostPopular(count);
    }

    @Transactional
    public Film addLike(Long filmId, Long userId) throws ValidationException {
        userRepository.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        Film film = filmRepository.getFilm(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
        filmRepository.addLike(filmId, userId);
        eventService.addEvent(userId, EventType.LIKE, Operation.ADD, filmId);
        return film;
    }

    @Transactional
    public Film removeLike(Long filmId, Long userId) throws ValidationException {
        userRepository.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        Film film = filmRepository.getFilm(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
        filmRepository.removeLike(filmId, userId);
        eventService.addEvent(userId, EventType.LIKE, Operation.REMOVE, filmId);
        return film;
    }

    private void validateFilmFields(Film film) throws ValidationException {
        if (film.getName() == null || film.getName().isBlank()) {
            log.error("Название фильма не может быть пустым");
            throw new ValidationException("Название фильма не может быть пустым");
        }
        if (film.getDescription() == null || film.getDescription().length() > Film.MAX_LENGTH_DESCRIPTION) {
            log.error("Длина описания больше {} символов", Film.MAX_LENGTH_DESCRIPTION);
            throw new ValidationException("Максимальная длина описания " + Film.MAX_LENGTH_DESCRIPTION + " символов");
        }
        if (film.getReleaseDate() == null || film.getReleaseDate().isBefore(Film.MIN_FILM_RELEASE_DATE)) {
            log.error("Дата релиза фильма  раньше {}",
                    Film.MIN_FILM_RELEASE_DATE.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
            throw new ValidationException("Дата релиза фильма не может быть раньше "
                    + Film.MIN_FILM_RELEASE_DATE.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
        }
        if (film.getDuration() == null || film.getDuration() <= 0) {
            log.error("Некорректная продолжительность фильма {} минут", film.getDuration());
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    public Collection<Film> findFilmsByDirector(Long directorId, String sortBy) {
        return filmRepository.findFilmsByDirector(directorId, sortBy);
    }

}
