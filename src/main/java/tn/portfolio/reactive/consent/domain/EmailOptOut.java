package tn.portfolio.reactive.consent.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import tn.portfolio.reactive.common.domain.Email;

import java.time.LocalDateTime;
import java.util.Objects;

@Table("email_opt_outs")
public class EmailOptOut {
    @Id
    private String email;

    private final LocalDateTime optedOutAt;

    public EmailOptOut(Email email, LocalDateTime optedOutAt) {
        this.email = email.value();
        this.optedOutAt = optedOutAt;
    }

    public EmailOptOut(String email, LocalDateTime optedOutAt) {
        this.email = email;
        this.optedOutAt = optedOutAt;
    }

    public static EmailOptOut optOut(Email email, LocalDateTime when) {
        return new EmailOptOut(email, when);
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
