package com.reactivespring;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.exception.ReviewsClientException;
import com.reactivespring.util.RetryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ReviewsRestClient {
    private WebClient webClient;
    @Value("${restClient.reviewsUrl}")
    private String reviewsUrl;

    ReviewsRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    private static Mono<? extends Throwable> status4xx(ClientResponse clientResponse) {
        log.info("Statuscode is: {}", clientResponse.statusCode().value());
        return clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)
                ? Mono.empty()
                : clientResponse.bodyToMono(String.class)
                .flatMap(responseMessage -> Mono.error(new ReviewsClientException(responseMessage)));
    }

    private static Mono<? extends Throwable> status5xx(ClientResponse clientResponse) {
        log.info("Statuscode is: {}", clientResponse.statusCode().value());
        return clientResponse.bodyToMono(String.class)
                .flatMap(responseMessage -> Mono.error(new MoviesInfoServerException(
                        "ServerException in ReviewsService: " + clientResponse)));
    }

    public Flux<Review> retrieveReviews(String movieId) {
        var url = UriComponentsBuilder
                .fromHttpUrl(reviewsUrl)
                .queryParam("movieInfoId", movieId)
                .buildAndExpand()
                .toUriString();

        return webClient
                .get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, ReviewsRestClient::status4xx
                )
                .onStatus(HttpStatus::is5xxServerError, ReviewsRestClient::status5xx)
                .bodyToFlux(Review.class)
                .retryWhen(RetryUtil.retrySpec())
                .log();
    }

}