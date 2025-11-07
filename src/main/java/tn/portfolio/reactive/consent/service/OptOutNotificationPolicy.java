package tn.portfolio.reactive.consent.service;

import reactor.core.publisher.Mono;
import tn.portfolio.reactive.common.domain.EmailAddress;
import tn.portfolio.reactive.consent.repository.EmailOptOutRepository;
import tn.portfolio.reactive.project.domain.EmailNotificationPolicy;

public class OptOutNotificationPolicy implements EmailNotificationPolicy {
    private final EmailOptOutRepository emailOptOuts;
    public OptOutNotificationPolicy(EmailOptOutRepository emailOptOuts) {
        this.emailOptOuts = emailOptOuts;
    }

    @Override
    public Mono<Boolean> notificationToEmailIsAllowed(EmailAddress emailAddress) {
        return emailOptOuts.existsByEmail(emailAddress.value())
                .map(optOut -> !optOut);
    }
}
