package tn.portfolio.reactive.project.service;

import reactor.core.publisher.Mono;
import tn.portfolio.reactive.common.domain.Email;

public interface EmailNotificationPolicy {
    Mono<Boolean> notificationToEmailIsAllowed(Email email);
}
