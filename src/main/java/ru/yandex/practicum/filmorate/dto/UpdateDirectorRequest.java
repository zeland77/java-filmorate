package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateDirectorRequest {
    @NotNull(message = "id режиссера должно быть указано")
    Long id;

    @NotBlank(message = "имя режиссера должно быть указано")
    @Size(max = 40, message = "длина имени превышает 40 символов")
    String name;
}
