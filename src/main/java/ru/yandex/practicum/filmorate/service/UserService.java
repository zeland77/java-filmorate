package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;

@Service
@Slf4j
public class UserService {
    UserStorage userStorage;

    public UserService(UserStorage friendStorage) {
        this.userStorage = friendStorage;
    }

    public Collection<User> friendsById(Long id) throws NotFoundException {
        User user = userStorage.getUser(id);
        return user.getFriends().stream().map(userStorage::getUser).toList();
    }

    public Collection<User> friendsCommon(Long id, Long otherId) {
        Collection<Long> idFriendUser = userStorage.getUser(id).getFriends();
        Collection<Long> idOtherUser = userStorage.getUser(otherId).getFriends();
        return idFriendUser.stream().filter(idOtherUser::contains).map(userStorage::getUser).toList();
    }

    public User addFriend(Long id, Long friendId) {
        User user = userStorage.getUser(id);
        user.getFriends().add(friendId);
        User friendUser = userStorage.getUser(friendId);
        friendUser.getFriends().add(id);
        return user;
    }

    public User deleteFriend(Long id, Long friendId) {
        User user = userStorage.getUser(id);
        user.getFriends().remove(friendId);
        User friendUser = userStorage.getUser(friendId);
        friendUser.getFriends().remove(id);
        return user;
    }
}