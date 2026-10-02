package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.repository.GenreRepository;

import java.util.Collection;

@Service
@Slf4j
public class GenreService {
    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public Collection<Genre> findAll() {
        return genreRepository.findAll();
    }

    public Genre getGenre(Long id) {
        return genreRepository.getGenre(id)
                .orElseThrow(() -> new NotFoundException("Жанр с id = " + id + " не найден"));
    }
}
