package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.repository.GenreRepository;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final EventService eventService;
    private final GenreRepository genreRepository;

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
                .orElseThrow(() -> new InternalServerException("Не удалось сохранить фильм " + film.getName()));
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

    public Collection<Film> mostPopular(Integer count, Long genreId, Integer year) throws ValidationException {
        if (count != null && count <= 0) {
            throw new ValidationException("Количество популярных фильмов должно быть положительным");
        }
        if (year != null && year < Film.MIN_FILM_RELEASE_DATE.getYear()) {
            throw new ValidationException("Год релиза фильма не может быть раньше "
                    + Film.MIN_FILM_RELEASE_DATE.getYear());
        }
        if (genreId != null && genreRepository.findAll().stream()
                .map(Genre::getId).filter(id -> id.equals(genreId))
                .findFirst()
                .isEmpty()) {
            throw new ValidationException("Жанр c id = " + genreId + "отсутствует");
        }
        return filmRepository.mostPopular(count, genreId, year);
    }

    public Collection<Film> commonFilms(Long userId, Long friendId) {
        userRepository.getUser(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id = " + userId + " не найден"));
        userRepository.getUser(friendId)
                .orElseThrow(() -> new NotFoundException(
                        "Пользователь с id = " + friendId + " не найден"));
        return filmRepository.commonFilms(userId, friendId);
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

    public Collection<Film> searchFilms(String query, List<String> by) {
        Set<String> searchFields = by.stream().map(String::toLowerCase).map(String::trim).collect(Collectors.toSet());
        return filmRepository.searchFilms(query, searchFields);
    }

}
