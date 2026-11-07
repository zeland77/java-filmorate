package ru.yandex.practicum.filmorate.repository;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

public interface ReviewRepository {

    Collection<Review> findAll();

    Optional<Review> getReview(Long reviewId);

    Collection<Review> getByFilmId(Long filmId, Long count);

    Collection<Review> getByCount(Long count);

    Optional<Review> create(Review review);

    Optional<Review> update(Review review);

    void delete(Long reviewId);

    Optional<Review> addLike(Long reviewId, Long userId);

    Optional<Review> addDislike(Long reviewId, Long userId);

    Optional<Review> deleteLike(Long reviewId, Long userId);

    Optional<Review> deleteDislike(Long reviewId, Long userId);

}
