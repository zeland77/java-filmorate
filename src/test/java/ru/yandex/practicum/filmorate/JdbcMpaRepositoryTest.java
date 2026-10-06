package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.repository.JdbcMpaRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@Import({JdbcMpaRepository.class, MpaRowMapper.class})
@DisplayName("JdbcMpaRepository")
public class JdbcMpaRepositoryTest {
    @Autowired
    JdbcMpaRepository mpaRepository;

    @Test
    @DisplayName("Рейтинг по ид")
    public void shouldReturnMpaWhenFindById() {
        Mpa testMpa = new Mpa();
        testMpa.setId(2L);
        testMpa.setName("PG");
        Optional<Mpa> mpaOptional = mpaRepository.getMpa(testMpa.getId());

        assertThat(mpaOptional)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(testMpa);
    }

    @Test
    @DisplayName("Все рейтинги")
    public void shouldReturnAllMpas() {
        Collection<Mpa> allMpas = new ArrayList<>();
        Mpa testMpa1 = new Mpa();
        testMpa1.setId(1L);
        testMpa1.setName("G");
        allMpas.add(testMpa1);
        Mpa testMpa2 = new Mpa();
        testMpa2.setId(2L);
        testMpa2.setName("PG");
        allMpas.add(testMpa2);
        Mpa testMpa3 = new Mpa();
        testMpa3.setId(3L);
        testMpa3.setName("PG-13");
        allMpas.add(testMpa3);
        Mpa testMpa4 = new Mpa();
        testMpa4.setId(4L);
        testMpa4.setName("R");
        allMpas.add(testMpa4);
        Mpa testMpa5 = new Mpa();
        testMpa5.setId(5L);
        testMpa5.setName("NC-17");
        allMpas.add(testMpa5);

        Collection<Mpa> mpa = mpaRepository.findAll();

        assertThat(mpa)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringExpectedNullFields()
                .isEqualTo(allMpas);
    }

}
