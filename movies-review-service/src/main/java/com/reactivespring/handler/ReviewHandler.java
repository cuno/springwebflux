package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ReviewHandler {
    private ReviewReactiveRepository reviewReactiveRepository;

    public ReviewHandler(ReviewReactiveRepository reviewReactiveRepository) {
        this.reviewReactiveRepository = reviewReactiveRepository;
    }

    public Mono<ServerResponse> addReview(ServerRequest request) {
        return request.bodyToMono(Review.class)
                .flatMap(reviewReactiveRepository::save)
                .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);
    }

    public Mono<ServerResponse> getReviews(ServerRequest request) {
        var movieInfoId = request.queryParam("movieInfoId");
        return movieInfoId.map(id -> {
                    var reviews = reviewReactiveRepository.findReviewsByMovieInfoId(Long.valueOf(id));
                    return buildReviewsResponse(reviews);
                })
                .orElseGet(() -> {
                    var reviews = reviewReactiveRepository.findAll();
                    return buildReviewsResponse(reviews);
                });
    }

    private static Mono<ServerResponse> buildReviewsResponse(Flux<Review> reviews) {
        return ServerResponse.ok().body(reviews, Review.class);
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");
        var existingReview = reviewReactiveRepository.findById(reviewId);
        return existingReview.flatMap(review -> request.bodyToMono(Review.class)
                        .map(reqReview -> {
                            review.setComment(reqReview.getComment());
                            review.setRating(reqReview.getRating());
                            return reqReview;
                        }))
                .flatMap(reviewReactiveRepository::save)
                .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview));
    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {
        var reviewId = request.pathVariable("id");
        var existingReview = reviewReactiveRepository.findById(reviewId);
        return existingReview.flatMap(review -> reviewReactiveRepository
                .deleteById(reviewId)
                .then(ServerResponse
                        .noContent()
                        .build()
                ));
    }
}
