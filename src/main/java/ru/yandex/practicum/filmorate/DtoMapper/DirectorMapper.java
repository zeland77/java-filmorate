package ru.yandex.practicum.filmorate.DtoMapper;

import ru.yandex.practicum.filmorate.dto.NewDirectorRequest;
import ru.yandex.practicum.filmorate.model.Director;

public class DirectorMapper {

    public static Director mapToDirector(NewDirectorRequest newDirector) {
        Director director = new Director();
        director.setId(newDirector.getId());
        director.setName(newDirector.getName());
        return director;
    }

}
