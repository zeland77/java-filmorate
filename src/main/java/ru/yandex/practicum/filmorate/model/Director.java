package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Director implements Comparable<Director> {

    @NotNull(message = "id режиссера должен быть указан")
    Long id;
    String name;

    @Override
    public int compareTo(Director o) {
        return Long.compare(this.id, o.id);
    }
}
