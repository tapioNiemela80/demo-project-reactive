package tn.portfolio.reactive.project.domain;

import tn.portfolio.reactive.common.domain.EmailAddress;
import tn.portfolio.reactive.common.domain.ValueObject;

import java.util.Objects;

@ValueObject
public final class ContactPerson {

    private final String name;
    private final EmailAddress emailAddress;

    private ContactPerson(String name, EmailAddress emailAddress) {
        this.name = name;
        this.emailAddress = emailAddress;
    }

    public static ContactPerson create(String name, String email) {
        return new ContactPerson(name, EmailAddress.of(email));
    }

    public static ContactPerson rehydrate(String name, String email) {
        return new ContactPerson(name, EmailAddress.rehydrate(email));
    }

    public String name() {
        return name;
    }

    public EmailAddress email() {
        return emailAddress;
    }

    public boolean hasValidEmail() {
        return emailAddress.isValid();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactPerson that = (ContactPerson) o;
        return name.equals(that.name) && emailAddress.equals(that.emailAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, emailAddress);
    }
}