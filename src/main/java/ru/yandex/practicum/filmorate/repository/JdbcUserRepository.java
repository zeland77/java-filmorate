package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository("jdbcUserRepository")
@RequiredArgsConstructor
@Slf4j
public class JdbcUserRepository implements UserRepository {
    private final NamedParameterJdbcOperations jdbc;
    private final UserRowMapper userRowMapper;

    public Collection<User> findAll() {
        List<User> users = jdbc.query("SELECT * FROM users", userRowMapper);
        return users;
    }

    public Optional<User> create(User user) {
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(user);
            jdbc.update("INSERT INTO users(email, login, name, birthday) VALUES (:email, :login, :name, :birthday)",
                    namedParameters,
                    keyHolder,
                    new String[]{"id"}
            );
            Long userId = keyHolder.getKey().longValue();
            return getUser(userId);
        } catch (DuplicateKeyException e) {
            return Optional.empty();
        }
    }

    public User delete(User user) throws ValidationException {
        jdbc.update("DELETE FROM likes WHERE user_id = :id",
                Map.of("id", user.getId())
        );
        jdbc.update("DELETE FROM friends WHERE user_id = :id",
                Map.of("id", user.getId())
        );
        jdbc.update("DELETE FROM users WHERE id = :id",
                Map.of("id", user.getId())
        );
        log.info("Удален пользователь {}", user);
        return user;
    }

    public Optional<User> update(User user) throws ValidationException {
        SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(user);
        jdbc.update("UPDATE users SET email = :email, login = :login, name = :name, birthday = :birthday WHERE id = :id",
                namedParameters
        );
        return getUser(user.getId());
    }

    public Optional<User> getUser(Long id) {
        List<User> results = jdbc.query(
                "SELECT * FROM users WHERE id = :id",
                Map.of("id", id),
                userRowMapper);
        return results.stream().findFirst();
    }

    public Optional<User> addFriend(User user, User friend) {
        jdbc.update("MERGE INTO friends (user_id, friend_id, friendship_id) KEY (user_id, friend_id)" +
                        " VALUES (:userId, :friendId, :friendshipId)",
                Map.of("userId", user.getId(),
                        "friendId", friend.getId(),
                        "friendshipId", 1));
        return getUser(user.getId());
    }

    public User removeFriend(User user, User friend) {
        jdbc.update("DELETE FROM friends WHERE user_id = :userId AND friend_id = :friendId",
                Map.of("userId", user.getId(),
                        "friendId", friend.getId()));
        return user;
    }

    public Collection<User> getFriends(User user) {
        List<User> friends = jdbc.query("SELECT * FROM users" +
                        " WHERE id IN (SELECT friend_id FROM friends WHERE user_id = :userId)",
                Map.of("userId", user.getId()),
                userRowMapper);
        return friends;
    }

}
