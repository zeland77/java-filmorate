package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.NewDirectorRequest;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/directors")
public class DirectorController {

    private final DirectorService directorService;

    @PostMapping
    public Director addDirector(@RequestBody @Valid NewDirectorRequest newDirector) {
        return directorService.addDirector(newDirector);
    }

    @GetMapping("/{id}")
    public Director findDirById(@PathVariable("id") Long dirId) {
        return directorService.findDirById(dirId);
    }

    @GetMapping
    public List<Director> findAllDirectors() {
        return directorService.findAllDirectors();
    }

    @PutMapping
    public Director updateDirector(@RequestBody @Valid NewDirectorRequest director) {
        return directorService.updateDirector(director);
    }

    @DeleteMapping("{id}")
    public void deleteDirector(@PathVariable("id") Long dirId) {
        directorService.deleteDirection(dirId);
    }
}
