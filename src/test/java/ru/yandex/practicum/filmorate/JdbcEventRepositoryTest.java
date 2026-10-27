package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.mapper.EventRowMapper;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.repository.JdbcEventRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({JdbcEventRepository.class, EventRowMapper.class})
@DisplayName("JdbcEventRepository")
public class JdbcEventRepositoryTest {

    @Test
    @DisplayName("Событие сохраняется и читается из базы")
    void shouldSaveAndReadEvent() {
        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "feed-test@example.com",
                "feed-test",
                "Пользователь для проверки ленты",
                java.sql.Date.valueOf("2000-03-20"));

        Long userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?",
                Long.class,
                "feed-test@example.com");

        Event event = new Event();
        event.setTimestamp(1700000000000L);
        event.setUserId(userId);
        event.setEventType(EventType.LIKE);
        event.setOperation(Operation.ADD);
        event.setEntityId(15L);

        Event savedEvent = eventRepository.create(event);

        assertThat(savedEvent.getEventId()).isNotNull();

        List<Event> events = eventRepository.findByUserId(userId);

        assertThat(events).hasSize(1);

        assertThat(events.get(0)).isEqualTo(savedEvent);
    }

    @Test
    @DisplayName("У пользователя без событий лента пустая")
    void shouldReturnEmptyFeedForUserWithoutEvents() {
        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "feed-test111@example.com",
                "feed-test111",
                "Пользователь для проверки ленты",
                java.sql.Date.valueOf("2000-03-20"));

        Long userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?",
                Long.class, "feed-test111@example.com");

        List<Event> events = eventRepository.findByUserId(userId);

        assertThat(events).isEmpty();
    }

    @Test
    @DisplayName("Лента содержит события только указанного пользователя")
    void shouldReturnOnlyRequestedUserEvents() {
        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "feed-test1@example.com",
                "feed-test1",
                "Пользователь для проверки ленты",
                java.sql.Date.valueOf("2000-03-20"));

        Long firstUserId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?",
                Long.class, "feed-test1@example.com");

        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "feed-test2@example.com",
                "feed-test2",
                "Пользователь для проверки ленты",
                java.sql.Date.valueOf("2000-03-20"));

        Long secondUserId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?",
                Long.class, "feed-test2@example.com");

        Event firstEvent = new Event();
        firstEvent.setTimestamp(2100000000000L);
        firstEvent.setUserId(firstUserId);
        firstEvent.setEventType(EventType.FRIEND);
        firstEvent.setOperation(Operation.REMOVE);
        firstEvent.setEntityId(10L);

        Event firstSaveEvent = eventRepository.create(firstEvent);

        Event secondEvent = new Event();
        secondEvent.setTimestamp(2000000000000L);
        secondEvent.setUserId(secondUserId);
        secondEvent.setEventType(EventType.FRIEND);
        secondEvent.setOperation(Operation.ADD);
        secondEvent.setEntityId(12L);

        eventRepository.create(secondEvent);

        List<Event> events = eventRepository.findByUserId(firstUserId);

        assertThat(events).hasSize(1);

        assertThat(events.get(0)).isEqualTo(firstSaveEvent);
    }

    @Test
    @DisplayName("События возвращаются от ранних к поздним")
    void shouldReturnEventsInChronologicalOrder() {
        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "feed-order@example.com",
                "feed-order",
                "Пользователь для проверки ленты",
                java.sql.Date.valueOf("2000-03-20"));

        Long userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?",
                Long.class, "feed-order@example.com");

        Event laterEvent = new Event();
        laterEvent.setTimestamp(1700000002000L);
        laterEvent.setUserId(userId);
        laterEvent.setEventType(EventType.LIKE);
        laterEvent.setOperation(Operation.REMOVE);
        laterEvent.setEntityId(15L);

        Event earlierEvent = new Event();
        earlierEvent.setTimestamp(1700000001000L);
        earlierEvent.setUserId(userId);
        earlierEvent.setEventType(EventType.LIKE);
        earlierEvent.setOperation(Operation.ADD);
        earlierEvent.setEntityId(15L);

        eventRepository.create(laterEvent);
        eventRepository.create(earlierEvent);

        List<Event> events = eventRepository.findByUserId(userId);

        assertThat(events).hasSize(2);

        assertThat(events.get(0)).isEqualTo(earlierEvent);

        assertThat(events.get(1)).isEqualTo(laterEvent);
    }


    @Autowired
    private JdbcEventRepository eventRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;
}
