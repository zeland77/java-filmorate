package ru.yandex.practicum.filmorate.DtoMapper;

import ru.yandex.practicum.filmorate.dto.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.model.Director;

public class DirectorMapper {

    public static Director mapToDirector(NewDirectorRequest newDirector) {
        Director director = new Director();
        director.setName(newDirector.getName());
        return director;
    }

    public static Director mapToDirector(UpdateDirectorRequest updatedDirector) {
        Director director = new Director();
        director.setId(updatedDirector.getId());
        director.setName(updatedDirector.getName());
        return director;
    }
}
