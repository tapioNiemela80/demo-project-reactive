package tn.portfolio.reactive.consent.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tn.portfolio.reactive.consent.repository.EmailOptOutRepository;
import tn.portfolio.reactive.consent.service.OptOutNotificationPolicy;
import tn.portfolio.reactive.project.domain.EmailNotificationPolicy;

@Configuration
public class ConsentConfiguration {
    @Bean
    public EmailNotificationPolicy emailNotificationPolicy(EmailOptOutRepository emailOptOuts){
        return new OptOutNotificationPolicy(emailOptOuts);
    }
}
