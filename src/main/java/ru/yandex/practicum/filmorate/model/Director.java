package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Data
public class Director implements Comparable<Director> {

    @NotNull(message = "id режиссера должен быть указан")
    Long id;
    @JsonProperty(access = READ_ONLY)
    String name;

    @Override
    public int compareTo(Director o) {
        return Long.compare(this.id, o.id);
    }

}
