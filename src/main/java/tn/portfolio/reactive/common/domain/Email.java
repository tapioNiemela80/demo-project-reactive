package tn.portfolio.reactive.common.domain;

import java.util.Objects;

@ValueObject
public final class Email {
    private final String value;
    private final boolean valid;

    private Email(String value, boolean valid) {
        this.value = value;
        this.valid = valid;
    }

    public static Email of(String value) {
        if (!EmailFormat.isValid(value)) {
            throw new EmailNotValidException("Email '%s' format is not valid".formatted(value));
        }
        return new Email(value, true);
    }

    public static Email rehydrate(String value) {
        return new Email(value, EmailFormat.isValid(value));
    }

    public boolean isValid() {
        return valid;
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return valid == email.valid && value.equals(email.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, valid);
    }
}