package tn.portfolio.reactive.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

@Component
public class ReactiveDomainEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(ReactiveDomainEventPublisher.class);
    private final Sinks.Many<Object> sink = Sinks.many().multicast().onBackpressureBuffer();

    public void publish(Object event) {
        Sinks.EmitResult result = sink.tryEmitNext(event);
        if (result.isFailure()) {
            log.error("Domain event {} dropped: {}", event, result);
        }
    }

    public <T> Flux<T> onEvent(Class<T> eventType) {
        return sink.asFlux().ofType(eventType);
    }
}