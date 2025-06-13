package tn.portfolio.reactive.common.service;

import reactor.core.publisher.Mono;
import tn.portfolio.reactive.common.EmailMessage;

public interface EmailClientService  {
    Mono<Void> send(EmailMessage message);
}