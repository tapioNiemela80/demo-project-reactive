package tn.portfolio.reactive.project.domain;

import tn.portfolio.reactive.common.domain.Email;
import tn.portfolio.reactive.common.domain.ValueObject;

import java.util.Objects;

@ValueObject
public final class ContactPerson {

    private final String name;
    private final Email email;

    private ContactPerson(String name, Email email) {
        this.name = name;
        this.email = email;
    }

    public static ContactPerson create(String name, String email) {
        return new ContactPerson(name, Email.of(email));
    }

    public static ContactPerson rehydrate(String name, String email) {
        return new ContactPerson(name, Email.rehydrate(email));
    }

    public String name() {
        return name;
    }

    public Email email() {
        return email;
    }

    public boolean hasValidEmail() {
        return email.isValid();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactPerson that = (ContactPerson) o;
        return name.equals(that.name) && email.equals(that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email);
    }
}