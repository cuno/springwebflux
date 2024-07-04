package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MoviesInfoControllerIntgTest {

    public static final String MOVIES_INFO_URL = "/v1/movieinfos";
    @Autowired
    MovieInfoRepository movieInfoRepository;

    @Autowired
    WebTestClient webTestClient;

    @BeforeAll
    static void beforeAll() {
        System.setProperty("os.arch", "x86_64");
    }

    @BeforeEach
    void setup() {
        var movieInfo = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));
        movieInfoRepository.deleteAll().block();
        movieInfoRepository
                .saveAll(movieInfo)
                .blockLast();
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void addMovieInfo() {
        // given
        var movieInfo = new MovieInfo(null, "Batman Begins again",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        // when
        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isCreated()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult ->
                {
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    // then
                    assert savedMovieInfo != null;
                    assert Objects.requireNonNull(savedMovieInfo).getMovieInfoId() != null;
                });

    }

    @Test
    void getAllMovieInfos() {
        // given
        // when
        webTestClient
                .get()
                .uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(3);
    }

    @Test
    void getMovieInfoById() {
        // given
        var movieInfoId = "abc";
        var title = "Dark Knight Rises";
        // when
        webTestClient
                .get()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
//                .jsonPath("$.name").isEqualTo()
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var movieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert movieInfo != null;
                    assert title.equals(movieInfo.getName());
                    assert 2012 == movieInfo.getYear();
                });
    }

    @Test
    void updateMovieInfo() {
        // given
        var movieInfoId = "abc";
        var title = "Dark Knight Rises Again";
        var movieInfo = new MovieInfo(null, title,
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));
        // when
        webTestClient
                .put()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult ->
                {
                    var updatedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    // then
                    assert updatedMovieInfo != null;
                    assert updatedMovieInfo.getMovieInfoId() != null;
                    assertEquals(title, updatedMovieInfo.getName());
                });

    }

    @Test
    void deleteMovieInfo() {
        // given
        var movieInfoId = "abc";
        // when
        webTestClient
                .delete()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                // then
                .isNoContent()
                .expectBody(Void.class);
    }
}