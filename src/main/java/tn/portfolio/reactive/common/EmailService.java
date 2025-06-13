package tn.portfolio.reactive.common;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class EmailService {

    public Mono<Void> sendEmail(String to, String subject, String body) {
        System.out.println("Sending email to " + to + " | Subject: " + subject + " | Body: " + body);
        return Mono.empty();
    }
}