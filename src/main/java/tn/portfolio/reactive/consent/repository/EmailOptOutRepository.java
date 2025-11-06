package tn.portfolio.reactive.consent.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;
import tn.portfolio.reactive.consent.domain.EmailOptOut;

public interface EmailOptOutRepository extends R2dbcRepository<EmailOptOut, String> {

    @Query("SELECT EXISTS(SELECT 1 FROM email_opt_outs WHERE email = :email)")
    Mono<Boolean> isOptedOut(String email);
}
