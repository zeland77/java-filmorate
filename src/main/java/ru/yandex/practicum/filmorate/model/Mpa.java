package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.READ_ONLY;

@Data
public class Mpa {
    @NotNull(message = "id рейтинга должен быть указан")
    private Long id;
    @JsonProperty(access = READ_ONLY)
    private String name;
}
