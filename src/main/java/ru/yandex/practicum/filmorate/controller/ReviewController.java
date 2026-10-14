package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;

@RestController
@RequestMapping("/reviews")
@Slf4j
public class ReviewController {
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/{id}")
    public Review getReview(@PathVariable("id") Long id) {
        return reviewService.getReview(id);
    }

    //"/reviews?filmId={filmId}&count={count}"
    @GetMapping()
    public Collection<Review> getReviewByFilmId(@RequestParam Long filmId,
                                               @RequestParam(defaultValue = "10") Long count) {
        return reviewService.getByFilmId(filmId, count);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Review create(@RequestBody Review review) {
        return reviewService.create(review);
    }

    @PutMapping
    public Review update(@RequestBody Review review) {
        return reviewService.update(review);
    }

    @PutMapping("/{id}/like/{userId}")
    public Review reviewAddLike(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        return reviewService.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public Review reviewAddDislike(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        return reviewService.addDislike(id, userId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable("id") Long id) {
        reviewService.delete(id);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public Review reviewDeleteLike(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        return reviewService.deleteLike(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public Review reviewDeleteDislike(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        return reviewService.deleteDislike(id, userId);
    }


}
