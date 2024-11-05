package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.UnaryOperator;

public class FluxAndMonoGeneratorService {
    private static Random random = new Random();

    public static Duration randomMillis() {
        return Duration.ofMillis(random.nextInt(500));
    }

    public Mono<String> namesMono(int minStrLen) {
        return Mono.just("Alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() >= minStrLen)
                .log();
    }

    public Mono<List<String>> namesMonoFlatmap(int minStrLen) {
        return Mono.just("Alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() >= minStrLen)
                .flatMap(this::splitStringMono)
                .log();
    }

    public Flux<String> namesMonoFlatmapMany(int minStrLen) {
        return Mono.just("Alex")
                .map(String::toUpperCase)
                .filter(s -> s.length() >= minStrLen)
                .flatMapMany(this::splitStringFlux)
                .log();
    }

    private Mono<List<String>> splitStringMono(String s) {
        return Mono.just(List.of(s.split("")));
    }

    private Flux<String> splitStringFlux(String s) {
        return Flux.fromArray(s.split(""));
    }

    public Flux<String> namesLength(int minStrLen) {
        return namesFlux()
                .map(String::toUpperCase)
                .filter(s -> s.length() >= minStrLen)
                .map(s -> s + " - " + s.length())
                .log();
    }

    public Flux<String> namesFlux() {
        return Flux.fromIterable(List.of("ALEX", "CHLOE"))
                .flatMap(FluxAndMonoGeneratorService::splitto)
                .log();
    }

    private static Flux<String> splitto(String name) {
        return Flux.fromArray(name.split(""));
    }

    private static Flux<String> splittoSlomo(String name) {
        return splitto(name).delayElements(randomMillis());
    }

    public Flux<String> namesFluxFlatmap(int minStrLen) {
        return Flux.fromIterable(List.of("alex", "chloe"))
                .map(String::toUpperCase)
                .filter(s -> s.length() >= minStrLen)
                .flatMap(this::splitStringFlux)
                .log();
    }

    public Flux<String> namesFluxTransform(int minStrLen) {
        UnaryOperator<Flux<String>> filterThenMap = name -> name
                .filter(s -> s.length() >= minStrLen)
                .map(String::toUpperCase);

        return Flux.fromIterable(List.of("alex", "chloe"))
                .transform(filterThenMap)
                .flatMap(this::splitStringFlux)
                .defaultIfEmpty("default")
                .log();
    }

    public Flux<String> namesFluxTransformSwitchIfEmpty(int minStrLen) {
        UnaryOperator<Flux<String>> toUpperChars = name -> name
                .map(String::toUpperCase)
                .flatMap(this::splitStringFlux);

        var defaultFlux = Flux.just("default")
                .transform(toUpperChars);

        return Flux.fromIterable(List.of("alex", "chloe"))
                .filter(s -> s.length() >= minStrLen)
                .transform(toUpperChars)
                .switchIfEmpty(defaultFlux)
                .log();
    }

    public Flux<String> exploreConcat() {
        var abcFlux = Flux.just("A", "B", "C");
        var defFlux = Flux.just("D", "E", "F");
        return Flux.concat(abcFlux, defFlux).log();
    }

    public Flux<String> exploreMergeWith() {
        var abcFlux = Flux.just("A", "B", "C").delayElements(Duration.ofMillis(100));
        var defFlux = Flux.just("D", "E", "F").delayElements(Duration.ofMillis(125));
        return abcFlux.mergeWith(defFlux).log();
    }

    public Flux<String> exploreMergeSequantial() {
        var abcFlux = Flux.just("A", "B", "C").delayElements(Duration.ofMillis(100));
        var defFlux = Flux.just("D", "E", "F").delayElements(Duration.ofMillis(125));
        return Flux.mergeSequential(abcFlux, defFlux).log();
    }

    public Flux<String> exploreZip() {
        var abcFlux = Flux.just("A", "B", "C");
        var defFlux = Flux.just("D", "E", "F");
        return Flux.zip(abcFlux, defFlux, (a, b) -> a + b).log();
    }

    public Flux<String> exploreZip_1() {
        var abcFlux = Flux.just("A", "B", "C");
        var defFlux = Flux.just("D", "E", "F");
        var _123Flux = Flux.just("1", "2", "3");
        var _456Flux = Flux.just("4", "5", "6");
        return Flux.zip(abcFlux, defFlux, _123Flux, _456Flux)
                .map(t4 -> t4.getT1() + t4.getT2() + t4.getT3() + t4.getT4())
                .log();
    }

    public Flux<String> exploreZipWith() {
        var abcFlux = Flux.just("A", "B", "C");
        var defFlux = Flux.just("D", "E", "F");
        return abcFlux
                .zipWith(defFlux, (a, b) -> a + b)
                .log();
    }

    public Mono<String> exploreZipWithMono() {
        var aMono = Mono.just("A");
        var bMono = Mono.just("B");
        return aMono
                .zipWith(bMono, (a, b) -> a + b)
                .log();
    }

    public Flux<String> exploreMergeWithMono() {
        var aMono = Mono.just("A");
        var bMono = Mono.just("B");
        return aMono.mergeWith(bMono).log();
    }

    public Flux<String> exploreConcatwith() {
        var aMono = Mono.just("A");
        var bMono = Mono.just("B");
        return aMono.concatWith(bMono).log();
    }

    public Flux<String> namesFluxFlatmapAsync() {
        return Flux.fromIterable(List.of("Cuno de Boer", "Iemand Anders"))
                .flatMap(FluxAndMonoGeneratorService::splittoSlomo)
                .log();
    }

    public Flux<String> namesFluxConcatmapAsync() {
        return Flux.fromIterable(List.of("Cuno de Boer", "Iemand Anders"))
                .concatMap(FluxAndMonoGeneratorService::splittoSlomo)
                .log();
    }

    public Flux<String> namesFluxImmutable() {
        return namesFlux()
                .log();
    }

    public static void main(String[] args) {
        FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();
        fluxAndMonoGeneratorService.namesFluxFlatmapAsync().subscribe(str -> System.out.println("Mono name is " + str));
    }

}
