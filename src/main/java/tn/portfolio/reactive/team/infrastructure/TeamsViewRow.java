package tn.portfolio.reactive.team.infrastructure;
import java.util.UUID;

record TeamsViewRow(
        UUID teamId,
        String teamName
) {}