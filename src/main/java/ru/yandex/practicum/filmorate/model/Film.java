package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Long duration;
    private Set<Long> likes = new HashSet<>();

    public static final int MAX_LENGTH_DESCRIPTION = 200;
    public static final LocalDate MIN_FILM_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    public void setLike(Long userId) {
        likes.add(userId);
    }

    public void removeLike(Long userId) {
        likes.remove(userId);
    }

    public int countLikes() {
        return likes.size();
    }
}
