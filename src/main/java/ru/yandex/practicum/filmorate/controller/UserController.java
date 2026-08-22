package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) throws ValidationException {
        validateUserFields(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Добавлен пользователь {}", user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) throws ValidationException {
        if (newUser.getId() == null) {
            log.error("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }
        validateUserFields(newUser);
        /*        if (users.containsKey(newUser.getId()) && !users.get(newUser.getId()).getEmail().equals(newUser.getEmail())) {
            if (users.values().stream().map(User::getEmail).anyMatch(email -> email.equals(newUser.getEmail()))) {
                log.error("Электронная почта {} уже используется", newUser.getEmail());
                throw new ValidationException("Эта электронная почта уже используется");
            }
        }*/
        User oldUser = users.get(newUser.getId());
        oldUser.setEmail(newUser.getEmail());
        oldUser.setLogin(newUser.getLogin());
        oldUser.setName(newUser.getName());
        oldUser.setBirthday(newUser.getBirthday());
        log.info("Обновлены данные пользователя {}", oldUser);
        return oldUser;

    }

    private void validateUserFields(User user) throws ValidationException {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error("Электронная почта не должна быть пустой");
            throw new ValidationException("Электронная почта не должна быть пустой");
        }
        if (!user.getEmail().contains("@")) {
            log.error("Некорректный адрес электронной почты: {}", user.getEmail());
            throw new ValidationException("Некорректный адрес электронной почты");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.error("Логин не должен быть пустым");
            throw new ValidationException("Логин не должен быть пустым");
        }
        if (user.getBirthday() == null) {
            log.error("Дата рождения не может быть пустой");
            throw new ValidationException("Дата рождения не может быть пустой");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.error("Дата рождения не может быть в будущем: {}", user.getBirthday());
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

}
