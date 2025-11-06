package tn.portfolio.reactive.project.service;

import reactor.core.publisher.Mono;
import tn.portfolio.reactive.common.domain.EmailAddress;

public interface EmailNotificationPolicy {
    Mono<Boolean> notificationToEmailIsAllowed(EmailAddress emailAddress);
}
