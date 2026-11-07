package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Director;
import java.util.List;

public interface DirectorRepository {

    Director addDirector(Director director);

    Director findDirById(Long dirId);

    List<Director> findAllDirectors();

    Director updateDirector(Director director);

    void deleteDirector(Long dirId);
}
