package tn.portfolio.reactive.common.domain;

import java.util.regex.Pattern;

final class EmailFormat {

    // This regex is commonly used and practically safe.
    // It blocks whitespace, commas, and enforces that the TLD is at least 2 characters long.
    private static final Pattern EMAIL_REGEX = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    private EmailFormat() {
        throw new UnsupportedOperationException("Utility class");
    }

    static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        return EMAIL_REGEX.matcher(value).matches();
    }
}