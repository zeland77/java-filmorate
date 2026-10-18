package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.repository.ReviewRepository;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.util.Collection;

@Service
@Slf4j
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository,
                         FilmRepository filmRepository,
                         UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.filmRepository = filmRepository;
        this.userRepository = userRepository;
    }

    public Review getReview(Long reviewId) {
        return reviewRepository.getReview(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id = " + reviewId + " не найден"));
    }

    public Collection<Review> getByFilmId(Long filmId, Long count) {
        if (count <= 0) {
            throw new ValidationException("Количество отзывов должно быть положительным");
        }
        if (filmId == null) {
            return reviewRepository.findAll();
        }
        filmRepository.getFilm(filmId)
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден"));

        return reviewRepository.getByFilmId(filmId, count);
    }

    public Review create(Review review) {
        validateReviewFields(review);
        return reviewRepository.create(review)
                .orElseThrow(() -> new ValidationException("Отзыв пользователя с Id " + review.getUserId()
                         + "для фильма c Id " + review.getFilmId() + " уже зарегистрирован"));
    }

    public Review update(Review review) {
        validateReviewFields(review);
        return reviewRepository.update(review)
                .orElseThrow(() -> new NotFoundException("Отзыв с reviewId = " + review.getReviewId() +
                        " для фильма filmId = " + review.getFilmId() +
                        ", оставленный userId = " + review.getUserId() + " не найден"));
    }

    public void delete(Long reviewId) {
        reviewRepository.delete(reviewId);
    }

    public Review addLike(Long reviewId, Long userId) {
        validateReviewIdAndUserId(reviewId, userId);
        reviewRepository.getReview(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id = " + reviewId + " не найден"));
        userRepository.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        return reviewRepository.addLike(reviewId, userId).get();
    }

    public Review addDislike(Long reviewId, Long userId) {
        validateReviewIdAndUserId(reviewId, userId);
        reviewRepository.getReview(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id = " + reviewId + " не найден"));
        userRepository.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        return reviewRepository.addDislike(reviewId, userId).get();
    }

    public Review deleteLike(Long reviewId, Long userId) {
        validateReviewIdAndUserId(reviewId, userId);
        reviewRepository.getReview(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id = " + reviewId + " не найден"));
        userRepository.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        return reviewRepository.deleteLike(reviewId, userId).get();
    }

    public Review deleteDislike(Long reviewId, Long userId) {
        validateReviewIdAndUserId(reviewId, userId);
        reviewRepository.getReview(reviewId)
                .orElseThrow(() -> new NotFoundException("Отзыв с id = " + reviewId + " не найден"));
        userRepository.getUser(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден"));
        return reviewRepository.deleteDislike(reviewId, userId).get();
    }

    private void validateReviewFields(Review review) throws ValidationException {
        if (review.getUserId() == null) {
            log.error("Id пользователя не может быть пустым");
            throw new ValidationException("Id пользователя не может быть пустым");
        }
        userRepository.getUser(review.getUserId())
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + review.getUserId() + " не найден"));

        if (review.getFilmId() == null) {
            log.error("Id фильма не может быть пустым");
            throw new ValidationException("Id фильма не может быть пустым");
        }
        filmRepository.getFilm(review.getFilmId())
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + review.getFilmId() + " не найден"));

        if (review.getContent() == null || review.getContent().isBlank()) {
            log.error("Отзыв не должен быть пустым или содержать пробелы");
            throw new ValidationException("Отзыв не должен быть пустым мли содержать пробелы");
        }

        if (review.getContent().length() > Review.MAX_LENGTH_CONTENT) {
            log.error("Отзыв не должен быть пустым или содержать пробелы");
            throw new ValidationException("Слишком большой отзыв. Сократите до " + Review.MAX_LENGTH_CONTENT + " символов");
        }

        if (review.getIsPositive() == null) {
            log.error("Тип отзыва не может быть пустой");
            throw new ValidationException("Тип отзыва не может быть пустой");
        }
    }

    private void validateReviewIdAndUserId(Long reviewId, Long userId) {
        if (reviewId == null) {
            log.error("Id отзыва не может быть пустым");
            throw new ValidationException("Id отзыва не может быть пустым");
        }
        if (userId == null) {
            log.error("Id пользователя не может быть пустым");
            throw new ValidationException("Id пользователя не может быть пустым");
        }
    }

}
