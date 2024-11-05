package com.reactivespring.controller;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Sinks;

import static reactor.core.publisher.Sinks.EmitFailureHandler.FAIL_FAST;

class SinkTest {

    @Test
    void sink() {
        //given
        Sinks.Many<Integer> replaySink = Sinks.many().replay().all();

        //when
        replaySink.emitNext(1, FAIL_FAST);
        replaySink.emitNext(2, FAIL_FAST);

        //then
        var integerFlux1 = replaySink.asFlux();
        integerFlux1.subscribe(i -> System.out.println("Subscriber 1: " + i));

        var integerFlux2 = replaySink.asFlux();
        integerFlux2.subscribe(i -> System.out.println("Subscriber 2: " + i));

        replaySink.tryEmitNext(3);
    }

    @Test
    void sinks_multicast() {
        // given`
        var multicast = Sinks.many().multicast().onBackpressureBuffer();

        // when
        multicast.emitNext(1, FAIL_FAST);
        multicast.emitNext(2, FAIL_FAST);

        // then
        var integerFlux1 = multicast.asFlux();
        integerFlux1.subscribe(i -> System.out.println("Subscriber 1: " + i));

        var integerFlux2 = multicast.asFlux();
        integerFlux2.subscribe(i -> System.out.println("Subscriber 2: " + i));

        multicast.emitNext(3, FAIL_FAST);
    }

    @Test
    void sinks_unicast() {
        // given`
        var multicast = Sinks.many().unicast().onBackpressureBuffer();

        // when
        multicast.emitNext(1, FAIL_FAST);
        multicast.emitNext(2, FAIL_FAST);

        // then
        var integerFlux1 = multicast.asFlux();
        integerFlux1.subscribe(i -> System.out.println("Subscriber 1: " + i));

        var integerFlux2 = multicast.asFlux();
        integerFlux2.subscribe(i -> System.out.println("Subscriber 2: " + i));

        multicast.emitNext(3, FAIL_FAST);

    }
}
