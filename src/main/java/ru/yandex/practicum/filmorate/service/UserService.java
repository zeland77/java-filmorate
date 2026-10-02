package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

@Service
@Slf4j
public class UserService {
    private final UserRepository userRepository;

    public UserService(@Qualifier("jdbcUserRepository") UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Collection<User> findAll() {
        return userRepository.findAll();
    }

    public User getUser(Long id) throws NotFoundException {
        return userRepository.getUser(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
    }

    public User create(User user) throws ValidationException {
        validateUserFields(user);
        return userRepository.create(user)
                .orElseThrow(() -> new ValidationException("Пользователь с таким email или логином уже зарегистрирован"));
    }

    public User update(User user) throws ValidationException {
        validateUserFields(user);
        return userRepository.update(user)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + user.getId() + " не найден"));
    }

    public User delete(User user) throws ValidationException {
        userRepository.delete(user);
        return user;
    }

    public Collection<User> friendsById(Long id) throws NotFoundException {
        User user = userRepository.getUser(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
        return userRepository.getFriends(user);
    }

    public Collection<User> friendsCommon(Long id, Long otherId) {
        User user = userRepository.getUser(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
        User otherUser = userRepository.getUser(otherId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + otherId + " не найден"));
        Collection<Long> idFriendUser = userRepository.getFriends(user).stream().map(User::getId).toList();
        Collection<Long> idOtherUser = userRepository.getFriends(otherUser).stream().map(User::getId).toList();
        return idFriendUser.stream()
                .filter(idOtherUser::contains)
                .map(userRepository::getUser)
                .flatMap(Optional::stream)
                .toList();
    }

    public User addFriend(Long userId, Long friendId) {
        User user = userRepository.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        User friend = userRepository.getUser(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + friendId + " не найден"));
        //user.getFriends().keySet().add(friendId);
        userRepository.addFriend(user, friend);
        return user;
    }

    public User removeFriend(Long userId, Long friendId) {
        User user = userRepository.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        User friend = userRepository.getUser(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + friendId + " не найден"));
        userRepository.removeFriend(user, friend);
        return user;
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
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
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

}
