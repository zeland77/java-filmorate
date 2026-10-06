package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Genre implements Comparable<Genre> {
    private Long id;
    private String name;

    @Override
    public int compareTo(Genre genre) {
        return Long.compare(this.id, genre.getId());
    }

}
