package tn.portfolio.reactive.team.domain;

import tn.portfolio.reactive.common.domain.ActualSpentTime;
import tn.portfolio.reactive.common.domain.AggregateRoot;
import tn.portfolio.reactive.project.domain.ProjectTaskId;
import tn.portfolio.reactive.team.infrastructure.TeamDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
@AggregateRoot
public class Team {
    private final TeamId id;
    private final String name;
    private final int version;
    private final List<TeamMember> members;
    private final List<TeamTask> tasks;

    public static Team createNew(TeamId id, String name) {
        return new Team(id, name, 0, new ArrayList<>(), new ArrayList<>());
    }

    public Team(TeamId id, String name, int version, List<TeamMember> members, List<TeamTask> tasks) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.members = members;
        this.tasks = tasks;
    }

    public TeamId getId() {
        return id;
    }

    public Team addMember(TeamMemberId memberId, String name, String profession){
        List<TeamMember> existingMembers = new ArrayList<>(members);
        existingMembers.add(TeamMember.createNew(memberId, name, profession));
        return new Team(id, this.name,  version,  existingMembers, this.tasks);
    }

    public Team removeMember(TeamMemberId memberId){
        Objects.requireNonNull(memberId);
        verifyContainsMember(memberId);
        verifyMemberCanBeRemoved(memberId);
        var remainingMembers = members.stream()
                .filter(member -> !member.hasId(memberId))
                .toList();
        return new Team(id, this.name,  version, remainingMembers, tasks);
    }

    private void verifyMemberCanBeRemoved(TeamMemberId memberId) {
        if(tasks.stream().anyMatch(task -> task.isAssignedTo(memberId))){
            throw new TeamMemberHasAssignedTasksException(memberId);
        }
    }

    private void verifyContainsMember(TeamMemberId memberId) {
        if(members.stream().noneMatch(member -> member.hasId(memberId))){
            throw new UnknownTeamMemberIdException(memberId);
        }
    }

    public boolean containsMember(TeamMemberId memberId, String name, String profession){
        return members.stream().anyMatch(member -> member.hasDetails(memberId, name, profession));
    }

    public boolean containsCompletedTask(TeamTaskId taskId, ProjectTaskId projectTaskId, String name, String description, ActualSpentTime actualSpentTime){
        return tasks.stream()
                .anyMatch(task -> task.hasDetails(taskId, projectTaskId, name, description, null, actualSpentTime, TeamTaskStatus.COMPLETED));
    }

    public boolean containsUncompletedTask(TeamTaskId taskId, ProjectTaskId projectTaskId, String name, String description, TeamMemberId assignee, TeamTaskStatus expectedStatus){
        return tasks.stream()
                .anyMatch(task -> task.hasDetails(taskId, projectTaskId, name, description, assignee, null, expectedStatus));
    }

    public Team addTask(TeamTaskId taskId, ProjectTaskId projectTaskId, String name, String description){
        List<TeamTask> existingTasks = new ArrayList<>(tasks);
        existingTasks.add(TeamTask.createNew(taskId, projectTaskId, name, description));
        return new Team(id, this.name,  version, this.members, existingTasks);
    }

    public Team removeTask(TeamTaskId taskId){
        verifyContainsTask(taskId);
        verifyTaskCanBeDeleted(taskId);
        var remainingTasks = tasks.stream()
                .filter(task -> !task.hasId(taskId))
                .toList();
        return new Team(id, this.name,  version, this.members, remainingTasks);
    }

    private void verifyTaskCanBeDeleted(TeamTaskId taskId) {
        var canBeDeleted = tasks.stream()
                .filter(task -> task.hasId(taskId))
                .map(TeamTask::canBeDeleted)
                .findFirst()
                .orElse(true);
        if(!canBeDeleted){
            throw new TaskCannotBeDeletedException(taskId);
        }
    }

    public Optional<ProjectTaskId> getOriginalTaskId(TeamTaskId taskId){
        return tasks.stream()
                .filter(task -> task.hasId(taskId))
                .map(TeamTask::getOriginalTaskId)
                .findFirst();
    }

    private void verifyContainsTask(TeamTaskId taskId){
        if(tasks.stream().noneMatch(task -> task.hasId(taskId))){
            throw new UnknownTeamTaskIdException(taskId);
        }
    }

    public Team assignTask(TeamTaskId taskId, TeamMemberId memberId) {
        verifyContainsTask(taskId);
        if(members.stream().noneMatch(member -> member.hasId(memberId))){
            throw new UnknownTeamMemberIdException(memberId);
        }
        var newTasks = tasks.stream()
                .map(assign(taskId, memberId))
                .toList();
        return new Team(id, name, version, members, newTasks);
    }

    public Team markTaskInProgress(TeamTaskId taskId) {
        verifyContainsTask(taskId);
        var newTasks = tasks.stream()
                .map(markInProgress(taskId))
                .toList();
        return new Team(id, name, version, members, newTasks);
    }

    public Team markTaskCompleted(TeamTaskId taskId, ActualSpentTime actualSpentTime) {
        verifyContainsTask(taskId);
        var newTasks = tasks.stream()
                .map(markCompleted(taskId, actualSpentTime))
                .toList();
        return new Team(id, name, version, members, newTasks);
    }

    public Team markTaskUnassigned(TeamTaskId taskId) {
        verifyContainsTask(taskId);
        var newTasks = tasks.stream()
                .map(markUnassigned(taskId))
                .toList();
        return new Team(id, name, version,  members, newTasks);
    }
    private Function<TeamTask, TeamTask> markUnassigned(TeamTaskId taskId) {
        return teamTask -> {
            if(teamTask.hasId(taskId)){
                return teamTask.unassign();
            }
            return teamTask;
        };
    }

    private Function<TeamTask, TeamTask> markInProgress(TeamTaskId taskId) {
        return teamTask -> {
            if(teamTask.hasId(taskId)){
                return teamTask.markInProgress();
            }
            return teamTask;
        };
    }

    private Function<TeamTask, TeamTask> markCompleted(TeamTaskId taskId, ActualSpentTime actualSpentTime) {
        return teamTask -> {
            if(teamTask.hasId(taskId)){
                return teamTask.complete(actualSpentTime);
            }
            return teamTask;
        };
    }

    private Function<TeamTask, TeamTask> assign(TeamTaskId taskId, TeamMemberId memberId) {
        return teamTask -> {
            if(teamTask.hasId(taskId)){
                return teamTask.assignTo(memberId);
            }
            return teamTask;
        };
    }


    public boolean isNew() {
        return version == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Team other = (Team) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    TeamDto toDto(){
        var dtoMembers = members.stream().map(member -> member.toDto(id)).toList();
        var dtoTasks = tasks.stream().map(member -> member.toDto(id)).toList();
        return new TeamDto(id.value(), name, version, dtoMembers, dtoTasks);
    }
}