package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Review {
    public static final int MAX_LENGTH_CONTENT = 300;
    private Long reviewId;
    private String content;
    private Boolean isPositive;
    private Long userId;
    private Long filmId;
    private Long useful;
}
