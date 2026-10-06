package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable("id") Long id) {
        return userService.getUser(id);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> friendsById(@PathVariable("id") Long id) {
        return userService.friendsById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody User user) {
        return userService.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        return userService.update(user);
    }

    @DeleteMapping
    public User delete(@Valid @RequestBody User user) {
        return userService.delete(user);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> friendsCommon(@PathVariable("id") Long id, @PathVariable("otherId") Long otherId) {
        return userService.friendsCommon(id, otherId);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public User addToFriends(@PathVariable("id") Long id, @PathVariable("friendId") Long friendId) {
        return userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public User deleteFromFriends(@PathVariable("id") Long id, @PathVariable("friendId") Long friendId) {
        return userService.removeFriend(id, friendId);
    }

}
