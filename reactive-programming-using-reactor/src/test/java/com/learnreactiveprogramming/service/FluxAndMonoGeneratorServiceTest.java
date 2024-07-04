package com.learnreactiveprogramming.service;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.List;

class FluxAndMonoGeneratorServiceTest {
    FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

    @Test
    void randomMillis() {
    }

    @Test
    void testNamesLength() {
    }

    @Test
    void testNamesFlux1() {
    }

    @Test
    void testNamesMono() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesMono(4))
                .expectNext("ALEX")
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void namesMonoFlatmap() {
        // given
        int minStrLen = 4;
        // when
        var value = fluxAndMonoGeneratorService.namesMonoFlatmap(minStrLen);
        // then
        StepVerifier.create(value)
                .expectNext(List.of("A", "L", "E", "X"))
                .verifyComplete();
    }

    @Test
    void namesMonoFlatmapMany() {
        // given
        int minStrLen = 4;
        // when
        var value = fluxAndMonoGeneratorService.namesMonoFlatmapMany(minStrLen);
        // then
        StepVerifier.create(value)
                .expectNext("A", "L", "E", "X")
                .verifyComplete();
    }

    @Test
    void namesFluxFlatmapAsync() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesFluxFlatmapAsync())
                .expectNextCount(25)
                .verifyComplete();
    }

    @Test
    void namesFluxConcatmapAsync() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesFluxConcatmapAsync())
                .expectNext("C", "u", "n", "o", " ", "d", "e", " ", "B", "o", "e", "r")
                .expectNext("I", "e", "m", "a", "n", "d", " ", "A", "n", "d", "e", "r", "s")
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void namesFluxImmutable() {
    }

    @Test
    void namesMono() {
    }

    @Test
    void main() {
    }

    @Test
    void namesFluxFlatmap() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesFluxFlatmap(5))
                .expectNext("C", "H", "L", "O", "E")
                .verifyComplete();
    }

    @Test
    void namesFluxTransform() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesFluxTransform(5))
                .expectNext("C", "H", "L", "O", "E")
                .verifyComplete();
    }

    @Test
    void namesFluxTransform_1() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesFluxTransform(9))
                .expectNext("default")
                .verifyComplete();
    }

    @Test
    void namesFluxTransformSwitchIfEmpty() {
        StepVerifier.create(fluxAndMonoGeneratorService.namesFluxTransformSwitchIfEmpty(9000))
                .expectNext("D", "E", "F", "A", "U", "L", "T")
                .verifyComplete();
    }

    @Test
    void exploreConcat() {
        // given
        // when
        var concatFlux = fluxAndMonoGeneratorService.exploreConcat();
        // then
        StepVerifier.create(concatFlux)
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();
    }

    @Test
    void exploreConcatwith() {
        // given
        // when
        var concatFlux = fluxAndMonoGeneratorService.exploreConcatwith();
        // then
        StepVerifier.create(concatFlux)
                .expectNext("A", "B")
                .verifyComplete();
    }

    @Test
    void exploreMergeWith() {
        // given
        // when
        var merged = fluxAndMonoGeneratorService.exploreMergeWith();
        // then
        StepVerifier.create(merged)
                .expectNext("A", "D", "B", "E", "C", "F")
                .verifyComplete();
    }

    @Test
    void exploreMergeWithMono() {
        // given
        // when
        var merged = fluxAndMonoGeneratorService.exploreMergeWithMono();
        // then
        StepVerifier.create(merged)
                .expectNext("A", "B")
                .verifyComplete();
    }

    @Test
    void exploreMergeSequantial() {
        // given
        // when
        var merged = fluxAndMonoGeneratorService.exploreMergeSequantial();
        // then
        StepVerifier.create(merged)
                .expectNext("A", "B", "C", "D", "E", "F")
                .verifyComplete();
    }

    @Test
    void exploreZip() {
        // given
        // when
        var merged = fluxAndMonoGeneratorService.exploreZip();
        // then
        StepVerifier.create(merged)
                .expectNext("AD", "BE", "CF")
                .verifyComplete();
    }

    @Test
    void exploreZip_1() {
        // given
        // when
        var merged = fluxAndMonoGeneratorService.exploreZip_1();
        // then
        StepVerifier.create(merged)
                .expectNext("AD14", "BE25", "CF36")
                .verifyComplete();
    }

    @Test
    void exploreZipWith() {
        // given
        // when
        var merged = fluxAndMonoGeneratorService.exploreZipWith();
        // then
        StepVerifier.create(merged)
                .expectNext("AD", "BE", "CF")
                .verifyComplete();
    }

    @Test
    void exploreZipWithMono() {
        // given
        // when
        var merged = fluxAndMonoGeneratorService.exploreZipWithMono();
        // then
        StepVerifier.create(merged)
                .expectNext("AB")
                .verifyComplete();
    }

}
