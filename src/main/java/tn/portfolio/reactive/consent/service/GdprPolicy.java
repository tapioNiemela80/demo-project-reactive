package tn.portfolio.reactive.consent.service;

import reactor.core.publisher.Mono;
import tn.portfolio.reactive.common.domain.Email;
import tn.portfolio.reactive.consent.repository.EmailOptOutRepository;
import tn.portfolio.reactive.project.service.EmailNotificationPolicy;

public class GdprPolicy implements EmailNotificationPolicy {
    private final EmailOptOutRepository emailOptOuts;
    public GdprPolicy(EmailOptOutRepository emailOptOuts) {
        this.emailOptOuts = emailOptOuts;
    }

    @Override
    public Mono<Boolean> notificationToEmailIsAllowed(Email email) {
        return emailOptOuts.isOptedOut(email.value())
                .map(optedOut -> !optedOut);
    }
}
