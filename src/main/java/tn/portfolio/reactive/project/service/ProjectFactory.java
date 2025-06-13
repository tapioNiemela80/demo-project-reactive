package tn.portfolio.reactive.project.service;

import org.springframework.stereotype.Component;
import tn.portfolio.reactive.common.service.DateService;
import tn.portfolio.reactive.project.controller.ContactPersonInput;
import tn.portfolio.reactive.project.domain.Project;
import tn.portfolio.reactive.project.domain.ProjectId;
import tn.portfolio.reactive.project.domain.TimeEstimation;

import java.time.LocalDate;

@Component
class ProjectFactory {
    private final DateService dateService;

    ProjectFactory(DateService dateService) {
        this.dateService = dateService;
    }

    Project createNew(ProjectId id, String name, String description,
                      LocalDate plannedEndDate,
                      tn.portfolio.reactive.project.controller.TimeEstimation timeEstimation,
                      ContactPersonInput contactPersonInput){
        System.out.println(id+" "+name+" "+description);
        return Project.create(id, name, description, dateService.now(), plannedEndDate, toDomain(timeEstimation), contactPersonInput.name(), contactPersonInput.email());
    }

    private TimeEstimation toDomain(tn.portfolio.reactive.project.controller.TimeEstimation timeEstimation) {
        return new TimeEstimation(timeEstimation.hours(), timeEstimation.minutes());
    }

}
