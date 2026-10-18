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
import ru.yandex.practicum.filmorate.mapper.ReviewRowMapper;
import ru.yandex.practicum.filmorate.model.*;

import java.util.*;

@Repository("jdbcReviewRepository")
@RequiredArgsConstructor
@Slf4j
public class JdbcReviewRepository implements ReviewRepository {
    private final NamedParameterJdbcOperations jdbc;
    private final ReviewRowMapper reviewRowMapper;

    public Collection<Review> findAll() {
        String sql = "SELECT" +
                " r.id, " +
                "r.content, " +
                "r.isPositive, " +
                "r.user_id, " +
                "r.film_id, " +
                "(SELECT COUNT(*) FROM reviews_likes rl WHERE rl.review_id = r.id) " +
                "- (SELECT COUNT(*) FROM reviews_dislikes rd WHERE rd.review_id = r.id) AS useful " +
                "FROM reviews AS r";
        return jdbc.query(sql, reviewRowMapper);
    }

    public Optional<Review> getReview(Long reviewId) {
        String sql = "SELECT" +
                " r.id, " +
                "r.content, " +
                "r.isPositive, " +
                "r.user_id, " +
                "r.film_id, " +
                "(SELECT COUNT(*) FROM reviews_likes rl WHERE rl.review_id = r.id) " +
                "- (SELECT COUNT(*) FROM reviews_dislikes rd WHERE rd.review_id = r.id) AS useful " +
                "FROM reviews AS r " +
                "WHERE r.id = :id";
        List<Review> results = jdbc.query(sql, Map.of("id", reviewId), reviewRowMapper);
        return results.stream().findFirst();
    }

    public Collection<Review> getByFilmId(Long filmId, Long count) {
        String sql = "SELECT" +
                " r.id, " +
                "r.content, " +
                "r.isPositive, " +
                "r.user_id, " +
                "r.film_id, " +
                "(SELECT COUNT(*) FROM reviews_likes rl WHERE rl.review_id = r.id) " +
                "- (SELECT COUNT(*) FROM reviews_dislikes rd WHERE rd.review_id = r.id) AS useful " +
                "FROM reviews AS r " +
                "WHERE r.film_id = :film_id " +
                "ORDER BY useful DESC " +
                "LIMIT :count";
        return jdbc.query(sql, Map.of("film_id", filmId, "count", count), reviewRowMapper);
    }

    public Optional<Review> create(Review review) {
        try {
            KeyHolder keyHolder = new GeneratedKeyHolder();
            SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(review);
            jdbc.update("INSERT INTO reviews(content, isPositive, user_id, film_id)" +
                            " VALUES (:content, :isPositive, :userId, :filmId)",
                    namedParameters,
                    keyHolder,
                    new String[]{"id"}
            );
            Long reviewId = keyHolder.getKey().longValue();
            return getReview(reviewId);
        } catch (DuplicateKeyException e) {
            log.error("Ошибка при создании отзыва. Отзыв пользователя id=" + review.getUserId()
                    + " на фильм id=" + review.getFilmId() + " уже существует.");
            return Optional.empty();
        }
    }

    public Optional<Review> update(Review review) {
        SqlParameterSource namedParameters = new BeanPropertySqlParameterSource(review);
        int rowUpdated = jdbc.update("UPDATE reviews SET content = :content, isPositive = :isPositive " +
                        "WHERE id = :reviewId AND user_id = :userId AND film_id = :filmId",
                namedParameters);
        if (rowUpdated > 0) {
            return getReview(review.getReviewId());
        }
        return Optional.empty();
    }

    public void delete(Long reviewId) {
        jdbc.update("DELETE FROM reviews WHERE id = :id",
                Map.of("id", reviewId)
        );
    }

    public Optional<Review> addLike(Long reviewId, Long userId) {
        jdbc.update("MERGE INTO reviews_likes (review_id, user_id) KEY (review_id, user_id)" +
                        " VALUES (:reviewId, :userId)",
                Map.of("reviewId", reviewId,"userId", userId));

        jdbc.update("DELETE FROM reviews_dislikes WHERE review_id = :reviewId AND user_id = :userId",
                Map.of("reviewId", reviewId,"userId", userId));

        return getReview(reviewId);
    }

    public Optional<Review> addDislike(Long reviewId, Long userId) {
        jdbc.update("MERGE INTO reviews_dislikes (review_id, user_id) KEY (review_id, user_id)" +
                        " VALUES (:reviewId, :userId)",
                Map.of("reviewId", reviewId,"userId", userId));

        jdbc.update("DELETE FROM reviews_likes WHERE review_id = :reviewId AND user_id = :userId",
                Map.of("reviewId", reviewId,"userId", userId));

        return getReview(reviewId);
    }

    public Optional<Review> deleteLike(Long reviewId, Long userId) {
        jdbc.update("DELETE FROM reviews_likes WHERE review_id = :reviewId AND user_id = :userId",
                Map.of("reviewId", reviewId,
                        "userId", userId));
        return getReview(reviewId);
    }

    public Optional<Review> deleteDislike(Long reviewId, Long userId) {
        jdbc.update("DELETE FROM reviews_dislikes WHERE review_id = :reviewId AND user_id = :userId",
                Map.of("reviewId", reviewId,
                        "userId", userId));
        return getReview(reviewId);
    }

}
