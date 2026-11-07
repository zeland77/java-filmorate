package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Data
public class Genre implements Comparable<Genre> {
    @NotNull(message = "id жанра должен быть указан")
    private Long id;
    @JsonProperty(access = READ_ONLY)
    private String name;

    @Override
    public int compareTo(Genre genre) {
        return Long.compare(this.id, genre.getId());
    }

}
