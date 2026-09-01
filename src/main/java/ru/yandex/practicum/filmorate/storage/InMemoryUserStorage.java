package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private Map<Long, User> users = new HashMap<>();

    public Collection<User> findAll() {
        return users.values();
    }

    public User create(@RequestBody User user) throws ValidationException {
        validateUserFields(user);
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Добавлен пользователь {}", user);
        return user;
    }

    public User delete(User user) throws ValidationException {
        log.info("Метод /delete ещё не реализован.");
        throw new ResponseStatusException(HttpStatus.NOT_IMPLEMENTED, "Метод /delete ещё не реализован.");
    }

    public User getUser(Long id) throws NotFoundException {
        if (users.values().stream().filter(user -> user.getId() == id).findFirst().isEmpty()) {
            log.error("Пользователь с id = {} не найден", id);
            throw new NotFoundException("Пользователь с id = " + id + " не найден");
        }
        return users.get(id);
    }

    public User update(@RequestBody User newUser) throws ValidationException {
        if (newUser.getId() == null) {
            log.error("Id должен быть указан");
            throw new ValidationException("Id должен быть указан");
        }
        validateUserFields(newUser);
        if (users.values().stream().filter(user -> user.getId() == newUser.getId()).findFirst().isEmpty()) {
            log.error("Пользователь с id = {} не найден", newUser.getId());
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
        }
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
        if (user.getLogin() == null || user.getLogin().contains(" ")) {
            log.error("Логин не должен быть пустым или содержать пробелы");
            throw new ValidationException("Логин не должен быть пусты мли содержать пробелы");
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
