package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;

@Data
public class Film {
    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Long duration;

    public static final int MAX_LENGTH_DESCRIPTION = 200;
    public static final LocalDate MIN_FILM_RELEASE_DATE = LocalDate.of(1895, 12, 28);
}
