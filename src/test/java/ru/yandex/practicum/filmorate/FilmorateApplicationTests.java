package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//@SpringBootTest
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmorateApplicationTests {
    private static final String URI_USERS = "/users";
    private static final String URI_FILMS = "/films";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private final Map<Long, User> users = mock(Map.class);

    @MockitoBean
    private final Map<Long, Film> films = new HashMap<>();

    @Test
    void shouldCreateUserSuccessfully() throws Exception {
        User requestDto = new User();
        requestDto.setName("Дмитрий");
        requestDto.setLogin("dima");
        requestDto.setEmail("dima@example.com");
        requestDto.setBirthday(LocalDate.of(1999, 12, 12));
        User responseDto = new User();
        responseDto.setId(1L);
        responseDto.setName("Дмитрий");
        responseDto.setLogin("dima");
        responseDto.setEmail("dima@example.com");
        responseDto.setBirthday(LocalDate.of(1999, 12, 12));

        Mockito.when(users.put(Mockito.any(Long.class), Mockito.any(User.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post(URI_USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    void shouldCreateUserWithNullName() throws Exception {
        User requestDto = new User();
        requestDto.setLogin("dima");
        requestDto.setEmail("dima@example.com");
        requestDto.setBirthday(LocalDate.of(1999, 12, 12));
        User responseDto = new User();
        responseDto.setId(1L);
        responseDto.setName("dima");
        responseDto.setLogin("dima");
        responseDto.setEmail("dima@example.com");
        responseDto.setBirthday(LocalDate.of(1999, 12, 12));

        Mockito.when(users.put(Mockito.any(Long.class), Mockito.any(User.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post(URI_USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    void shouldCreateUserWithIncorrectEmail() throws Exception {
        User requestDto = new User();
        requestDto.setName("Дмитрий");
        requestDto.setLogin("dima");
        requestDto.setEmail("dimaexample.com");
        requestDto.setBirthday(LocalDate.of(1999, 12, 12));

        mockMvc.perform(MockMvcRequestBuilders.post(URI_USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(result -> Assertions.assertInstanceOf(
                        ValidationException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    Assertions.assertEquals("Некорректный адрес электронной почты",
                            exception.getMessage());
                });
    }

    @Test
    void shouldCreateUserWithNullEmail() throws Exception {
        User requestDto = new User();
        requestDto.setName("Дмитрий");
        requestDto.setLogin("dima");
        requestDto.setBirthday(LocalDate.of(1999, 12, 12));

        mockMvc.perform(MockMvcRequestBuilders.post(URI_USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(result -> Assertions.assertInstanceOf(
                        ValidationException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    Assertions.assertEquals("Электронная почта не должна быть пустой",
                            exception.getMessage());
                });
    }

    @Test
    void shouldCreateUserWithNullLogin() throws Exception {
        User requestDto = new User();
        requestDto.setName("Дмитрий");
        requestDto.setEmail("dima@example.com");
        requestDto.setBirthday(LocalDate.of(1999, 12, 12));

        mockMvc.perform(MockMvcRequestBuilders.post(URI_USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(result -> Assertions.assertInstanceOf(
                        ValidationException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    Assertions.assertEquals("Логин не должен быть пусты мли содержать пробелы",
                            exception.getMessage());
                });
    }

    @Test
    void shouldCreateUserWithSpaceLogin() throws Exception {
        User requestDto = new User();
        requestDto.setLogin("dima petrov");
        requestDto.setName("Дмитрий");
        requestDto.setEmail("dima@example.com");
        requestDto.setBirthday(LocalDate.of(1999, 12, 12));

        mockMvc.perform(MockMvcRequestBuilders.post(URI_USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(result -> Assertions.assertInstanceOf(
                        ValidationException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    Assertions.assertEquals("Логин не должен быть пусты мли содержать пробелы",
                            exception.getMessage());
                });
    }

    @Test
    void shouldCreateUserWithIncorrectBirthDay() throws Exception {
        User requestDto = new User();
        requestDto.setName("Дмитрий");
        requestDto.setLogin("dima");
        requestDto.setEmail("dima@example.com");
        requestDto.setBirthday(LocalDate.now().plusDays(1));

        mockMvc.perform(MockMvcRequestBuilders.post(URI_USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(result -> Assertions.assertInstanceOf(
                        ValidationException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    Assertions.assertEquals("Дата рождения не может быть в будущем",
                            exception.getMessage());
                });
    }

    @Test
    void shouldCreateUserWithNullBirthDay() throws Exception {
        User requestDto = new User();
        requestDto.setName("Дмитрий");
        requestDto.setLogin("dima");
        requestDto.setEmail("dima@example.com");

        mockMvc.perform(MockMvcRequestBuilders.post(URI_USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(result -> Assertions.assertInstanceOf(
                        ValidationException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    Assertions.assertEquals("Дата рождения не может быть пустой",
                            exception.getMessage());
                });
    }

    @Test
    void shouldUpdateUserSuccessfully() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("Владимир");
        user.setLogin("vova");
        user.setEmail("vova@example.com");
        user.setBirthday(LocalDate.of(2000, 2, 2));

        User requestDto = new User();
        requestDto.setId(1L);
        requestDto.setName("Дмитрий");
        requestDto.setLogin("dima");
        requestDto.setEmail("dima@example.com");
        requestDto.setBirthday(LocalDate.of(1999, 12, 12));
        User responseDto = new User();
        responseDto.setId(1L);
        responseDto.setName("Дмитрий");
        responseDto.setLogin("dima");
        responseDto.setEmail("dima@example.com");
        responseDto.setBirthday(LocalDate.of(1999, 12, 12));

        Mockito.when(users.put(Mockito.any(Long.class), Mockito.any(User.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post(URI_USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(put(URI_USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    void shouldUpdateUserWithNullId() throws Exception {
        User requestDto = new User();
        requestDto.setName("Дмитрий");
        requestDto.setLogin("dima");
        requestDto.setEmail("dimaexample.com");
        requestDto.setBirthday(LocalDate.of(1999, 12, 12));

        mockMvc.perform(MockMvcRequestBuilders.put(URI_FILMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(result -> Assertions.assertInstanceOf(
                        ValidationException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    Assertions.assertEquals("Id должен быть указан",
                            exception.getMessage());
                });
    }

    @Test
    void shouldGetAllUser() throws Exception {
        User requestDto1 = new User();
        requestDto1.setName("Дмитрий");
        requestDto1.setLogin("dima");
        requestDto1.setEmail("dima@example.com");
        requestDto1.setBirthday(LocalDate.of(1999, 12, 12));
        User requestDto2 = new User();
        requestDto2.setName("Дмитрий");
        requestDto2.setLogin("dima");
        requestDto2.setEmail("dima@example.com");
        requestDto2.setBirthday(LocalDate.of(1999, 12, 12));

        User responseDto1 = new User();
        responseDto1.setId(1L);
        responseDto1.setName("Дмитрий");
        responseDto1.setLogin("dima");
        responseDto1.setEmail("dima@example.com");
        responseDto1.setBirthday(LocalDate.of(1999, 12, 12));
        User responseDto2 = new User();
        responseDto2.setId(2L);
        responseDto2.setName("Дмитрий");
        responseDto2.setLogin("dima");
        responseDto2.setEmail("dima@example.com");
        responseDto2.setBirthday(LocalDate.of(1999, 12, 12));

        Collection<User> collection = new ArrayList<>();
        collection.add(responseDto1);
        collection.add(responseDto2);

        Mockito.when(users.values()).thenReturn(collection);

        mockMvc.perform(post(URI_USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto1)))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(post(URI_USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto2)))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get(URI_USERS)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(collection)));
    }

    @Test
    void shouldCreateFilmSuccessfully() throws Exception {
        Film requestDto = new Film();
        requestDto.setName("Титаник");
        requestDto.setDescription("rrtytrtuytjuyjghfggdsfg");
        requestDto.setReleaseDate(LocalDate.of(1997, 12, 12));
        requestDto.setDuration(109L);
        Film responseDto = new Film();
        responseDto.setId(1L);
        responseDto.setName("Титаник");
        responseDto.setDescription("rrtytrtuytjuyjghfggdsfg");
        responseDto.setReleaseDate(LocalDate.of(1997, 12, 12));
        responseDto.setDuration(109L);

        Mockito.when(films.put(Mockito.any(Long.class), Mockito.any(Film.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post(URI_FILMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    void shouldCreateFilmWithNullName() throws Exception {
        Film requestDto = new Film();
        requestDto.setDescription("rrtytrtuytjuyjghfggdsfg");
        requestDto.setReleaseDate(LocalDate.of(1997, 12, 12));
        requestDto.setDuration(109L);

        mockMvc.perform(MockMvcRequestBuilders.post(URI_FILMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(result -> Assertions.assertInstanceOf(
                        ValidationException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    Assertions.assertEquals("Название фильма не может быть пустым",
                            exception.getMessage());
                });
    }

    @Test
    void shouldCreateFilmWithLongDescription() throws Exception {
        Film requestDto = new Film();
        requestDto.setName("Титаник");
        requestDto.setDescription("a".repeat(Film.MAX_LENGTH_DESCRIPTION + 1));
        requestDto.setReleaseDate(LocalDate.of(1997, 12, 12));
        requestDto.setDuration(109L);

        mockMvc.perform(MockMvcRequestBuilders.post(URI_FILMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(result -> Assertions.assertInstanceOf(
                        ValidationException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    Assertions.assertEquals("Максимальная длина описания "
                                    + Film.MAX_LENGTH_DESCRIPTION + " символов",
                            exception.getMessage());
                });
    }

    @Test
    void shouldCreateFilmWithMinDateRelease() throws Exception {
        Film requestDto = new Film();
        requestDto.setName("Титаник");
        requestDto.setDescription("rrtytrtuytjuyjghfggdsfg");
        requestDto.setReleaseDate(LocalDate.of(1895, 12, 27));
        requestDto.setDuration(109L);

        mockMvc.perform(MockMvcRequestBuilders.post(URI_FILMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(result -> Assertions.assertInstanceOf(
                        ValidationException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    Assertions.assertEquals("Дата релиза фильма не может быть раньше "
                                    + Film.MIN_FILM_RELEASE_DATE.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                            exception.getMessage());
                });
    }

    @Test
    void shouldCreateFilmWithNegativeDuration() throws Exception {
        Film requestDto = new Film();
        requestDto.setName("Титаник");
        requestDto.setDescription("rrtytrtuytjuyjghfggdsfg");
        requestDto.setReleaseDate(LocalDate.of(1997, 12, 12));
        requestDto.setDuration(-1L);

        mockMvc.perform(MockMvcRequestBuilders.post(URI_FILMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(result -> Assertions.assertInstanceOf(
                        ValidationException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    Assertions.assertEquals("Продолжительность фильма должна быть положительным числом",
                            exception.getMessage());
                });
    }

    @Test
    void shouldUpdateFilmSuccessfully() throws Exception {
        Film film = new Film();
        film.setId(1L);
        film.setName("Достать ножи");
        film.setDescription("укукукуку  уеккекен екненгнг");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(111L);

        Film requestDto = new Film();
        requestDto.setId(1L);
        requestDto.setName("Титаник");
        requestDto.setDescription("rrtytrtuytjuyjghfggdsfg");
        requestDto.setReleaseDate(LocalDate.of(1997, 12, 12));
        requestDto.setDuration(109L);
        Film responseDto = new Film();
        responseDto.setId(1L);
        responseDto.setName("Титаник");
        responseDto.setDescription("rrtytrtuytjuyjghfggdsfg");
        responseDto.setReleaseDate(LocalDate.of(1997, 12, 12));
        responseDto.setDuration(109L);

        Mockito.when(films.put(Mockito.any(Long.class), Mockito.any(Film.class)))
                .thenReturn(responseDto);

        mockMvc.perform(post(URI_FILMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(put(URI_FILMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(responseDto)));
    }

    @Test
    void shouldUpdateFilmWithoutId() throws Exception {
        Film requestDto = new Film();
        requestDto.setName("Титаник");
        requestDto.setDescription("rrtytrtuytjuyjghfggdsfg");
        requestDto.setReleaseDate(LocalDate.of(1997, 12, 12));
        requestDto.setDuration(100L);

        mockMvc.perform(MockMvcRequestBuilders.put(URI_FILMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(result -> Assertions.assertInstanceOf(
                        ValidationException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    Assertions.assertEquals("Id должен быть указан",
                            exception.getMessage());
                });
    }

    @Test
    void shouldUpdateFilmNotFoundId() throws Exception {
        Film requestDto = new Film();
        requestDto.setId(5L);
        requestDto.setName("Титаник");
        requestDto.setDescription("rrtytrtuytjuyjghfggdsfg");
        requestDto.setReleaseDate(LocalDate.of(1997, 12, 12));
        requestDto.setDuration(100L);

        mockMvc.perform(MockMvcRequestBuilders.put(URI_FILMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(result -> Assertions.assertInstanceOf(
                        NotFoundException.class,
                        result.getResolvedException()))
                .andExpect(result -> {
                    Exception exception = result.getResolvedException();
                    Assertions.assertEquals("Фильм с id = " + requestDto.getId() + " не найден",
                            exception.getMessage());
                });
    }

    @Test
    void shouldGetAllFilms() throws Exception {
        Film requestDto1 = new Film();
        requestDto1.setName("Титаник");
        requestDto1.setDescription("rrtytrtuytjuyjghfggdsfg");
        requestDto1.setReleaseDate(LocalDate.of(1997, 12, 12));
        requestDto1.setDuration(100L);
        Film requestDto2 = new Film();
        requestDto2.setName("Титаник");
        requestDto2.setDescription("rrtytrtuytjuyjghfggdsfg");
        requestDto2.setReleaseDate(LocalDate.of(1997, 12, 12));
        requestDto2.setDuration(100L);

        Film responseDto1 = new Film();
        responseDto1.setId(1L);
        responseDto1.setName("Титаник");
        responseDto1.setDescription("rrtytrtuytjuyjghfggdsfg");
        responseDto1.setReleaseDate(LocalDate.of(1997, 12, 12));
        responseDto1.setDuration(100L);
        Film responseDto2 = new Film();
        responseDto2.setId(2L);
        responseDto2.setName("Титаник");
        responseDto2.setDescription("rrtytrtuytjuyjghfggdsfg");
        responseDto2.setReleaseDate(LocalDate.of(1997, 12, 12));
        responseDto2.setDuration(100L);

        Collection<Film> collection = new ArrayList<>();
        collection.add(responseDto1);
        collection.add(responseDto2);

        Mockito.when(films.values()).thenReturn(collection);

        mockMvc.perform(post(URI_FILMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto1)))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(post(URI_FILMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto2)))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get(URI_FILMS)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().json(objectMapper.writeValueAsString(collection)));
    }

}
