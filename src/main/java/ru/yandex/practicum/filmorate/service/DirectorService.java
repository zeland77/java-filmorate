package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.DtoMapper.DirectorMapper;
import ru.yandex.practicum.filmorate.dto.NewDirectorRequest;
import ru.yandex.practicum.filmorate.dto.UpdateDirectorRequest;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.repository.DirectorRepository;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorRepository directorRepository;

    public Director addDirector(NewDirectorRequest newDirectorRq) {
        Director newDirector = DirectorMapper.mapToDirector(newDirectorRq);
        return directorRepository.addDirector(newDirector);
    }

    public Director findDirById(Long dirId) {
        return directorRepository.findDirById(dirId);
    }

    public List<Director> findAllDirectors() {
        return directorRepository.findAllDirectors();
    }

    public Director updateDirector(UpdateDirectorRequest newDirectorRq) {
        Director newDirector = DirectorMapper.mapToDirector(newDirectorRq);
        return directorRepository.updateDirector(newDirector);
    }

    public void deleteDirection(Long dirId) {
        directorRepository.deleteDirector(dirId);
    }
}
