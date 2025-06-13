package tn.portfolio.reactive.team.controller;

import java.util.UUID;

public record MemberView(
        UUID id,
        String name,
        String profession
) {}