package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface FilmRepository {
    Collection<Film> findAll();

    Optional<Film> create(Film film);

    Film delete(Film film);

    Optional<Film> update(Film newFilm);

    Optional<Film> getFilm(Long id);

    void addLike(Long filmId, Long userId);

    void removeLike(Long filmId, Long userId);

    Collection<Film> mostPopular(Integer count);

    Collection<Film> mostPopular(Integer count, Long genreId, Integer year);

    Collection<Film> findFilmsByDirector(Long directorId, String sortBy);

    Collection<Film> searchFilms(String query, Set<String> searchFields);
  
    Collection<Film> commonFilms(Long userId, Long friendId);
}
