package tn.portfolio.reactive.common;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
public class ReactiveDomainEventPublisher {

    private final Sinks.Many<Object> sink = Sinks.many().multicast().onBackpressureBuffer();

    public void publish(Object event) {
        sink.tryEmitNext(event);
    }

    public <T> Flux<T> onEvent(Class<T> eventType) {
        return sink.asFlux().ofType(eventType);
    }
}