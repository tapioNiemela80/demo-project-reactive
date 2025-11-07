package tn.portfolio.reactive.consent.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import tn.portfolio.reactive.common.domain.EmailAddress;

import java.time.LocalDateTime;
import java.util.Objects;

@Table("email_opt_outs")
public class EmailOptOut {
    @Id
    private String email;

    private final LocalDateTime optedOutAt;

    public EmailOptOut(EmailAddress emailAddress, LocalDateTime optedOutAt) {
        this.email = emailAddress.value();
        this.optedOutAt = optedOutAt;
    }

    public static EmailOptOut optOut(EmailAddress emailAddress, LocalDateTime when) {
        return new EmailOptOut(emailAddress, when);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailOptOut other = (EmailOptOut) o;
        return Objects.equals(email, other.email);
    }

    @Override
    public int hashCode() {
        return email.hashCode();
    }
}
