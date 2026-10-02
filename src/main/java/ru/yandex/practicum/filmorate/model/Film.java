package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

@Data
public class Film {
    public static final int MAX_LENGTH_DESCRIPTION = 200;
    public static final LocalDate MIN_FILM_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Long duration;
    private Mpa mpa;
    private Set<Genre> genres = new TreeSet<>();
    private Set<Long> likes = new HashSet<>();

    public void addGenre(Genre genre) {
        genres.add(genre);
    }

    public void removeGenre(Genre genre) {
        genres.remove(genre);
    }

    public void addLike(Long userId) {
        likes.add(userId);
    }

    public void removeLike(Long userId) {
        likes.remove(userId);
    }

    public int countLikes() {
        return likes.size();
    }
}
