package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface EventRepository {
    Event create(Event event);

    List<Event> findByUserId(Long userId);
}
