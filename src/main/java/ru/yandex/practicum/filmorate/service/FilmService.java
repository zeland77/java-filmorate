package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.repository.GenreRepository;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.time.format.DateTimeFormatter;
import java.util.Collection;

@Service
@Slf4j
public class FilmService {
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final GenreRepository genreRepository;

    public FilmService(FilmRepository filmRepository, UserRepository userRepository, GenreRepository genreRepository) {
        this.filmRepository = filmRepository;
        this.userRepository = userRepository;
        this.genreRepository = genreRepository;
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
        return filmRepository.update(film)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + film.getId() + " не найден"));
    }

    public Film delete(Film film) {
        return filmRepository.delete(film);
    }

    public Collection<Film> mostPopular(Integer count, Long genreId, Integer year) throws ValidationException {
        if (count <= 0) {
            throw new ValidationException("Количество популярных фильмов должно быть положительным");
        }
        if (genreId == null || year == null) {
            return filmRepository.mostPopular(count);
        }
        if (year < Film.MIN_FILM_RELEASE_DATE.getYear()) {
            throw new ValidationException("Год релиза фильма не может быть раньше "
                    + Film.MIN_FILM_RELEASE_DATE.getYear());
        }
        if (genreRepository.findAll().stream()
                .map(Genre::getId).filter(id -> id == genreId)
                .findFirst()
                .isEmpty()) {
            throw new ValidationException("Жанр c id = " + genreId + "отсутствует");
        }
        return filmRepository.mostPopular(count, genreId, year);
    }

    public Film addLike(Long filmId, Long userId) throws ValidationException {
        userRepository.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        Film film = filmRepository.getFilm(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
        filmRepository.addLike(filmId, userId);
        film.addLike(userId);
        return film;
    }

    public Film removeLike(Long filmId, Long userId) throws ValidationException {
        userRepository.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        Film film = filmRepository.getFilm(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));
        filmRepository.removeLike(filmId, userId);
        film.removeLike(userId);
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

}
