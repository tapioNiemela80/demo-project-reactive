package tn.portfolio.reactive.common.domain;

import java.util.Objects;

@ValueObject
public final class EmailAddress {
    private final String value;
    private final boolean valid;

    private EmailAddress(String value, boolean valid) {
        this.value = value;
        this.valid = valid;
    }

    public static EmailAddress of(String value) {
        if (!EmailFormat.isValid(value)) {
            throw new EmailNotValidException("Email '%s' format is not valid".formatted(value));
        }
        return new EmailAddress(value.toLowerCase(), true);
    }

    public static EmailAddress rehydrate(String value) {
        return new EmailAddress(value.toLowerCase(), EmailFormat.isValid(value));
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
        EmailAddress emailAddress = (EmailAddress) o;
        return valid == emailAddress.valid && value.equals(emailAddress.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, valid);
    }
}