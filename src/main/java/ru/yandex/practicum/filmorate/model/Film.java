package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.Valid;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;
import java.util.TreeSet;

@Data
public class Film {
    public static final int MAX_LENGTH_DESCRIPTION = 200;
    public static final LocalDate MIN_FILM_RELEASE_DATE = LocalDate.of(1895, 12, 28);

    private Long id;
    private String name;
    private String description;
    private LocalDate releaseDate;
    private Long duration;

    @Valid
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    private Mpa mpa = new Mpa();

    @JsonSetter(nulls = Nulls.AS_EMPTY, contentNulls = Nulls.FAIL)
    @Valid
    private Set<Genre> genres = new TreeSet<>();

    @Valid
    @JsonSetter(nulls = Nulls.AS_EMPTY, contentNulls = Nulls.FAIL)
    private Set<Director> directors = new TreeSet<>();

    public void addDirector(Director director) {
        this.directors.add(director);
    }

    public void addGenre(Genre genre) {
        genres.add(genre);
    }

}
