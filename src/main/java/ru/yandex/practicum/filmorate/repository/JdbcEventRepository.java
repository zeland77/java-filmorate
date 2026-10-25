package ru.yandex.practicum.filmorate.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mapper.EventRowMapper;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class JdbcEventRepository implements EventRepository {
    private final NamedParameterJdbcOperations jdbc;
    private final EventRowMapper eventRowMapper;

    @Override
    public List<Event> findByUserId(Long userId) {
        String findEventsByUserIdSql = "SELECT * FROM events WHERE user_id = :userId "
                + "ORDER BY timestamp ASC, event_id ASC";
        return jdbc.query(findEventsByUserIdSql, Map.of("userId", userId), eventRowMapper);
    }

    @Override
    public Event create(Event event) {
        String createSql = "INSERT INTO events (timestamp, user_id, event_type, operation, entity_id)\n" +
                "VALUES (:timestamp, :userId, :eventType, :operation, :entityId)";
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("timestamp", event.getTimestamp());
        parameters.addValue("userId", event.getUserId());
        parameters.addValue("eventType", event.getEventType().name());
        parameters.addValue("operation", event.getOperation().name());
        parameters.addValue("entityId", event.getEntityId());
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(createSql, parameters, keyHolder, new String[]{"event_id"});
        Number generatedKey = keyHolder.getKey();

        if (generatedKey == null) {
            throw new IllegalStateException("База данных не вернула ID события");
        }

        event.setEventId(generatedKey.longValue());
        return event;
    }
}
