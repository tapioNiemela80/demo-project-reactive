package tn.portfolio.reactive.common.domain;

import java.util.regex.Pattern;

final class EmailFormat {

    // Tämä regex on yleisesti käytetty ja turvallinen käytännön tasolla.
    // Se estää mm. whitespace, pilkut, ja pakottaa TLD:n vähintään 2 merkkiin.
    private static final Pattern EMAIL_REGEX = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    private EmailFormat() {
        // estää instanssien luonnin → tämä on tarkoituksella "funktio-tyyppinen" osa domainia
    }

    static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        return EMAIL_REGEX.matcher(value).matches();
    }
}