package tn.portfolio.reactive.team.view;

import java.util.UUID;

public record MemberView(
        UUID id,
        String name,
        String profession
) {}