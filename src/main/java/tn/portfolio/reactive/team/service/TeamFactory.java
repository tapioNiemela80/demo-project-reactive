package tn.portfolio.reactive.team.service;

import org.springframework.stereotype.Component;
import tn.portfolio.reactive.team.domain.Team;
import tn.portfolio.reactive.team.domain.TeamId;

@Component
class TeamFactory {
    Team create(TeamId id, String name){
        return Team.createNew(id, name);
    }
}
