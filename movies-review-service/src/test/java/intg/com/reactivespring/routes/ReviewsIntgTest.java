package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class ReviewsIntgTest {
    private static final String REVIEWS_URL = "/v1/reviews";
    @Autowired
    ReviewReactiveRepository reviewReactiveRepository;

    @Autowired
    WebTestClient webTestClient;

    @BeforeAll
    static void beforeAll() {
        System.setProperty("os.arch", "x86_64");
    }

    @BeforeEach
    void setUp() {
        var reviewsList = List.of(
                new Review(null, 1L, "Movie 1", 9.0),
                new Review("xyz", 2L, "Movie 2", 9.0),
                new Review(null, 3L, "Movie 3", 8.0),
                new Review(null, 1L, "Movie 1 again", 8.0)
        );
        reviewReactiveRepository.deleteAll().block();
        reviewReactiveRepository.saveAll(reviewsList).blockLast();
    }

    @AfterEach
    void tearDown() {
        reviewReactiveRepository.deleteAll().block();
    }

    @Test
    void addReview() {
        // given
        var review = new Review(null, 1L, "Movie 1", 9.0);
        // when
        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(Review.class)
                .consumeWith(movieInfoEntityExchangeResult ->
                {
                    var savedReview = movieInfoEntityExchangeResult.getResponseBody();
                    // then
                    assert savedReview != null;
                    assert Objects.requireNonNull(savedReview).getMovieInfoId() != null;
                });
    }

    @Test
    void updateReview() {
        // given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);
        var savedReview = reviewReactiveRepository.save(review).block();
        var reviewUpdate = new Review(null, 1L, "Not an Awesome Movie", 8.0);
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
                    assertEquals(8.0, updatedReview.getRating());
                    assertEquals("Not an Awesome Movie", updatedReview.getComment());
                });

    }

    @Test
    void deleteReview() {
        // given
        var review = new Review(null, 1L, "Awesome Movie", 9.0);
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

        // when
        webTestClient
                .get()
                .uri(uriBuilder -> uriBuilder.path(REVIEWS_URL)
                        .queryParam("movieInfoId", "1")
                        .build())
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                // then
                .hasSize(2);
    }

}
