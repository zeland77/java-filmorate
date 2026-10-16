package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewDirectorRequest {
    Long id;
    @NotBlank(message = "имя режиссера должно быть указано")
    String name;
}
