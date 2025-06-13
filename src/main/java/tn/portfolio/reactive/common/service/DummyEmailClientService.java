package tn.portfolio.reactive.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import tn.portfolio.reactive.common.EmailMessage;

@Service
public class DummyEmailClientService implements EmailClientService {
    private static final Logger log = LoggerFactory.getLogger(DummyEmailClientService.class);

    @Override
    public Mono<Void> send(EmailMessage message) {
        return Mono.just(message)
                .doOnNext(emailMessage -> log.info(message.toString()))
                .then();
    }
}
