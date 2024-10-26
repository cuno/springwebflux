package com.reactivespring.controller;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084)
@TestPropertySource(properties = {
        "restClient.moviesInfoUrl = http://localhost:8084/v1/movieinfos",
        "restClient.reviewsUrl = http://localhost:8084/v1/reviews"
})
class MoviesControllerIntgTest {

    public static final String MOVIES_URL = "/v1/movies";

    @Autowired
    WebTestClient webTestClient;

    @BeforeAll
    static void beforeAll() {
        System.setProperty("os.arch", "x86_64");
    }

    @AfterEach
    void afterEach() {
        WireMock.reset();
    }

    @Test
    void retrieveMovieById() {
        // given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));

        stubFor(get(urlEqualTo("/v1/reviews?movieInfoId=" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));

        // when
        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var movie = movieEntityExchangeResult.getResponseBody();
                    // then
                    assert Objects.requireNonNull(movie).getReviewList().size() == 2;
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                });
    }


    @Test
    void retrieveMovieById_404() {
        // given
        var movieId = "abc";
        var url = "/v1/movieinfos/" + movieId;
        stubFor(get(urlEqualTo(url))
                .willReturn(aResponse()
                        .withStatus(404)));

        stubFor(get(urlEqualTo("/v1/reviews?movieInfoId=" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));

        // when
        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus()
                .is4xxClientError();
//                .expectBody(String.class)
//                .isEqualTo("some error");
        WireMock.verify(1, getRequestedFor(urlEqualTo(url)));
    }

    @Test
    void retrieveMovieById_reviews_404() {
        // given
        var movieId = "abc";
        stubFor(get(urlEqualTo("/v1/movieinfos/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));


        stubFor(get(urlEqualTo("/v1/reviews?movieInfoId=" + movieId))
                .willReturn(aResponse()
                        .withStatus(404)));

        // when
        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(Movie.class)
                .consumeWith(movieEntityExchangeResult -> {
                    var movie = movieEntityExchangeResult.getResponseBody();
                    // then
                    assert Objects.requireNonNull(movie).getReviewList().isEmpty();
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                });
    }


    @Test
    void retrieveMovieById_5XX() {
        // given
        var movieId = "abc";
        var url = "/v1/movieinfos/" + movieId;
        stubFor(get(urlEqualTo(url))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Movieinfoservice unavailable")));

        // when
        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus()
                .is5xxServerError();
//                .expectBody(String.class)
//                .isEqualTo("some error");

        WireMock.verify(4, getRequestedFor(urlEqualTo(url)));
    }


    @Test
    void retrieveMovieById_reviews_5XX() {
        // given
        var movieId = "abc";

        stubFor(get(urlEqualTo("/v1/movieinfos/" + movieId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));

        stubFor(get(urlEqualTo("/v1/reviews/"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Review service unavailable")));

        // when
        webTestClient
                .get()
                .uri("/v1/movies/{id}", movieId)
                .header("Content-Type", "application/json")
                .exchange()
                .expectStatus()
                .is5xxServerError();
//                .expectBody(String.class)
//                .isEqualTo("some error");

        WireMock.verify(4, getRequestedFor(urlPathMatching("/v1/reviews*")));
    }
}