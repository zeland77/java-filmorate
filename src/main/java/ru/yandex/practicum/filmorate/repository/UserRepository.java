package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserRepository {
    Collection<User> findAll();

    Optional<User> create(User user) throws ValidationException;

    User delete(User user) throws ValidationException;

    Optional<User> update(User newUser) throws ValidationException;

    Optional<User> getUser(Long id);

    Optional<User> addFriend(User user, User friend);

    User removeFriend(User user, User friend);

    Collection<User> getFriends(User user);
}
