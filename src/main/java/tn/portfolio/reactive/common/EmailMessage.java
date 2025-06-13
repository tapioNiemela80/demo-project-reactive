package tn.portfolio.reactive.common;

public record EmailMessage(String from, String to, String subject, String content, boolean isHtml) {

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
            """.formatted(from, to, subject, isHtml, content);
    }
}