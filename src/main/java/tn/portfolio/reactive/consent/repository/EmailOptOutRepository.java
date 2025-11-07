package tn.portfolio.reactive.consent.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;
import tn.portfolio.reactive.consent.domain.EmailOptOut;

public interface EmailOptOutRepository extends R2dbcRepository<EmailOptOut, String> {

    Mono<Boolean> existsByEmail(String email);

}
