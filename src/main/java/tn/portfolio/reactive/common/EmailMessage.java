package tn.portfolio.reactive.common;

import tn.portfolio.reactive.common.domain.EmailAddress;

public record EmailMessage(EmailAddress from, EmailAddress to, String subject, String content, boolean isHtml) {

    @Override
    public String toString() {
        return """
            === DUMMY EMAIL ===
            From: %s
            To: %s
            Subject: %s
            Is HTML: %s
            Content:
            %s
            ====================
            """.formatted(from.value(), to.value(), subject, isHtml, content);
    }
}