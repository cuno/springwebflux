package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
import com.reactivespring.router.ReviewRouter;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class})
@AutoConfigureWebTestClient
public class ReviewsUnitTest {
    @MockBean
    private ReviewReactiveRepository reviewRepository;

    @Autowired
    WebTestClient webTestClient;

    private static final String REVIEWS_URL = "/v1/reviews";
    @Autowired
    private ReviewReactiveRepository reviewReactiveRepository;

    @Captor
    ArgumentCaptor<Long> movieIdCaptor;

    private static Flux<Review> getReviewsFlux() {
        var reviewsFlux = Flux.just(
                new Review(null, 1L, "Movie 1", 9.0),
                new Review("xyz", 2L, "Movie 2", 9.0),
                new Review(null, 3L, "Movie 3", 8.0),
                new Review(null, 1L, "Movie 1 again", 8.0)
        );
        return reviewsFlux;
    }

    @Test
    void addReview() {
        // given
        var review = new Review("abc", 2L, "Movie 1", 9.0);
        when(reviewReactiveRepository.save(isA(Review.class)))
                .thenReturn(
                        Mono.just(review)
                );
        // when
        webTestClient.post().uri(REVIEWS_URL).bodyValue(review).exchange().expectStatus().isCreated().expectBody(Review.class).consumeWith(movieInfoEntityExchangeResult -> {
            var savedReview = movieInfoEntityExchangeResult.getResponseBody();
            // then
            assert savedReview != null;
            assertEquals(2L, savedReview.getMovieInfoId());
            assertEquals("abc", savedReview.getReviewId());
            assertEquals("Movie 1", savedReview.getComment());
            assertEquals(9d, savedReview.getRating());
        });
    }

    @Test
    void updateReview() {
        // given
        var review = new Review("abc", 2L, "Movie 1", 9.0);
        when(reviewReactiveRepository.save(isA(Review.class)))
                .thenReturn(
                        Mono.just(review)
                );
        when(reviewReactiveRepository.findById(anyString()))
                .thenReturn(
                        Mono.just(review)
                );
        var savedReview = reviewReactiveRepository.save(review).block();
        var reviewUpdate = new Review(null, 1L, "Not an Awesome Movie", 1.0);
        // when
        assert savedReview != null;

        webTestClient
                .put()
                .uri(REVIEWS_URL + "/{id}", savedReview.getReviewId())
                .bodyValue(reviewUpdate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Review.class)
                .consumeWith(reviewResponse -> {
                    var updatedReview = reviewResponse.getResponseBody();
                    assert updatedReview != null;
                    assertNotNull(savedReview.getReviewId());
                    assertEquals(1.0, updatedReview.getRating());
                    assertEquals("Not an Awesome Movie", updatedReview.getComment());
                });

    }

    @Test
    void deleteReview() {
        // given
        var id = "abc";
        var review = new Review(id, 2L, "Movie 1", 9.0);
        when(reviewReactiveRepository.save(isA(Review.class)))
                .thenReturn(
                        Mono.just(review)
                );
        when(reviewReactiveRepository.findById(id))
                .thenReturn(
                        Mono.just(review)
                );
        when(reviewReactiveRepository.deleteById(id))
                .thenReturn(Mono.empty());
        var savedReview = reviewReactiveRepository.save(review).block();
        // when
        assert savedReview != null;
        webTestClient
                .delete()
                .uri(REVIEWS_URL + "/{id}", savedReview.getReviewId())
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void getReviews() {
        // given
        when(reviewReactiveRepository.findAll())
                .thenReturn(getReviewsFlux());
        // when
        webTestClient
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .hasSize(4);
    }

    @Test
    void getReviewsByMovieInfoId() {
        // given
        when(reviewReactiveRepository.findReviewsByMovieInfoId(anyLong()))
                .thenReturn(getReviewsFlux().filter(review -> Objects.equals(1L, review.getMovieInfoId())));
        // when
        webTestClient
                .get()
                .uri(uriBuilder -> {
                    return uriBuilder.path(REVIEWS_URL)
                            .queryParam("movieInfoId", "1")
                            .build();
                })
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                // then
                .hasSize(2);
    }

}
