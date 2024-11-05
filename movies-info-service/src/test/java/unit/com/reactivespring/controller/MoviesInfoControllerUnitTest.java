package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import com.reactivespring.service.MovieInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = MoviesInfoController.class)
@AutoConfigureWebTestClient
public class MoviesInfoControllerUnitTest {
    public static final String MOVIES_INFO_URL = "/v1/movieinfos";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private MovieInfoService movieInfoServiceMock;

    @MockBean
    private MovieInfoRepository movieInfoRepository;

    @Test
    void getAllMoviesInfo() {
        // given
        var movieInfo = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        // when
        when(movieInfoServiceMock.getAllMovieInfos()).thenReturn(Flux.fromIterable(movieInfo));
        when(movieInfoRepository.findAll()).thenReturn(Flux.fromIterable(movieInfo));

        // then
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
    void addMovieInfo() {
        // given
        var movieInfo = new MovieInfo(null, "Batman Begins again",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        // when
        when(movieInfoServiceMock.addMovieInfo(isA(MovieInfo.class))).thenReturn(Mono.just(new MovieInfo("mockId", "Batman Begins once again",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"))));

        // then
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
                    assertEquals("mockId", savedMovieInfo.getMovieInfoId());
                });

    }

    @Test
    void addMovieInfo_validation() {
        // given
        var movieInfo = new MovieInfo(null, "",
                -2005, List.of("", "Michael Cane"), LocalDate.parse("2005-06-15"));

        // then
        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(stringEntityExchangeResult -> {
                    var responseBody = stringEntityExchangeResult.getResponseBody();
                    System.out.println("responseBody: " + responseBody);
                    assert responseBody != null;
                    var expectedErrorMessage = "movieInfo.cast must be present,movieInfo.name must be present,movieInfo.year must be a positive value";
                    assertEquals(expectedErrorMessage, responseBody);
                });
    }

    @Test
    void getMovieInfoById() {
        // given
        var movieInfoId = "abc";
        var title = "Dark Knight Rises";
        var movieInfo = new MovieInfo("abc", title,
                2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"));

        // when
        when(movieInfoServiceMock.getMovieInfoById(movieInfoId)).thenReturn(Mono.just(movieInfo));

        // then
        webTestClient
                .get()
                .uri(MOVIES_INFO_URL + "/{id}", movieInfoId)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var movieInfoRetreived = movieInfoEntityExchangeResult.getResponseBody();
                    assert movieInfoRetreived != null;
                    assert title.equals(movieInfoRetreived.getName());
                    assert 2012 == movieInfoRetreived.getYear();
                });

    }

    @Test
    void deleteMovieInfoById() {
        // given
        var movieInfoId = "abc";
        when(movieInfoServiceMock.deleteMovieInfoById(isA(String.class)))
                .thenReturn(Mono.empty());
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
