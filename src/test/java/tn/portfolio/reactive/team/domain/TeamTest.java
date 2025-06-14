package tn.portfolio.reactive.team.domain;

import org.junit.jupiter.api.Test;
import tn.portfolio.reactive.common.domain.ActualSpentTime;
import tn.portfolio.reactive.project.domain.ProjectTaskId;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeamTest {
    @Test
    void addsMember(){
        Team team = Team.createNew(getTeamId(), "project team");
        TeamMemberId teamMemberId = getMemberId();
        Team afterAdd = team.addMember(teamMemberId, "John doe", "tester");
        assertFalse(team.containsMember(teamMemberId, "John doe", "tester"));
        assertTrue(afterAdd.containsMember(teamMemberId, "John doe", "tester"));
    }

    @Test
    void removesMember(){
        Team team = Team.createNew(getTeamId(), "project team");
        TeamMemberId teamMemberId = getMemberId();
        Team afterAdd = team.addMember(teamMemberId, "John doe", "tester");
        assertFalse(team.containsMember(teamMemberId, "John doe", "tester"));
        assertTrue(afterAdd.containsMember(teamMemberId, "John doe", "tester"));
        Team afterRemove = afterAdd.removeMember(teamMemberId);
        assertFalse(afterRemove.containsMember(teamMemberId, "John doe", "tester"));
    }

    @Test
    void throwsExceptionWhenTryingToRemoveUnknownMember(){
        Team team = Team.createNew(getTeamId(), "project team");
        TeamMemberId teamMemberId = getMemberId();
        assertThrows(UnknownTeamMemberIdException.class, () -> team.removeMember(teamMemberId));
    }

    @Test
    void addsTask(){
        Team team = Team.createNew(getTeamId(), "project team");
        TeamTaskId taskId = getTaskId();
        ProjectTaskId projectTaskId = getProjectTaskId();
        Team afterAdd = team.addTask(taskId, projectTaskId, "test", "robot framework");
        assertFalse(team.containsUncompletedTask(taskId, projectTaskId, "test", "robot framework", null, TeamTaskStatus.NOT_ASSIGNED));
        assertTrue(afterAdd.containsUncompletedTask(taskId, projectTaskId, "test", "robot framework", null, TeamTaskStatus.NOT_ASSIGNED));
    }

    @Test
    void givesOriginalProjectTaskId(){
        Team team = Team.createNew(getTeamId(), "project team");
        TeamTaskId taskId = getTaskId();
        ProjectTaskId projectTaskId = getProjectTaskId();
        assertTrue(team.getOriginalTaskId(taskId).isEmpty());
        Team afterAdd = team.addTask(taskId, projectTaskId, "test", "robot framework");
        assertEquals(projectTaskId, afterAdd.getOriginalTaskId(taskId).get());
    }

    @Test
    void removesTask(){
        Team team = Team.createNew(getTeamId(), "project team");
        TeamTaskId taskId = getTaskId();
        TeamTaskId taskId2 = getTaskId();
        ProjectTaskId projectTaskId = getProjectTaskId();
        ProjectTaskId projectTaskId2 = getProjectTaskId();
        Team afterAdd = team.addTask(taskId, projectTaskId, "test", "robot framework");
        afterAdd = afterAdd.addTask(taskId2, projectTaskId2, "test", "unit test");
        assertTrue(afterAdd.containsUncompletedTask(taskId, projectTaskId, "test", "robot framework", null, TeamTaskStatus.NOT_ASSIGNED));
        assertTrue(afterAdd.containsUncompletedTask(taskId2, projectTaskId2, "test", "unit test", null, TeamTaskStatus.NOT_ASSIGNED));
        Team afterRemoval = afterAdd.removeTask(taskId);
        assertFalse(afterRemoval.containsUncompletedTask(taskId, projectTaskId, "test", "robot framework", null, TeamTaskStatus.NOT_ASSIGNED));
        assertTrue(afterRemoval.containsUncompletedTask(taskId2, projectTaskId2, "test", "unit test", null, TeamTaskStatus.NOT_ASSIGNED));
    }

    @Test
    void throwsExceptionWhenTryingToRemoveNonExistingTask(){
        Team team = Team.createNew(getTeamId(), "project team");
        TeamTaskId taskId = getTaskId();
        assertThrows(UnknownTeamTaskIdException.class, () -> team.removeTask(taskId));
    }

    @Test
    void throwsExceptionWhenTryingToRemoveTaskWhichCannotBeDeleted(){
        Team team = Team.createNew(getTeamId(), "project team");
        TeamTaskId taskId = getTaskId();
        TeamTaskId taskId2 = getTaskId();
        TeamMemberId memberId = getMemberId();
        ProjectTaskId projectTaskId = getProjectTaskId();
        ProjectTaskId projectTaskId2 = getProjectTaskId();
        Team afterAdd = team.addTask(taskId, projectTaskId, "test", "robot framework");
        afterAdd = afterAdd.addMember(memberId, "john doe", "tester");
        afterAdd = afterAdd.addTask(taskId2, projectTaskId2, "test", "unit test");
        assertTrue(afterAdd.containsUncompletedTask(taskId, projectTaskId, "test", "robot framework", null, TeamTaskStatus.NOT_ASSIGNED));
        assertTrue(afterAdd.containsUncompletedTask(taskId2, projectTaskId2, "test", "unit test", null, TeamTaskStatus.NOT_ASSIGNED));
        final Team teamWithAssignedTask = afterAdd.assignTask(taskId, memberId);
        assertThrows(TaskCannotBeDeletedException.class, () -> teamWithAssignedTask.removeTask(taskId));

        assertTrue(teamWithAssignedTask.containsUncompletedTask(taskId, projectTaskId, "test", "robot framework", memberId, TeamTaskStatus.ASSIGNED));
        assertTrue(teamWithAssignedTask.containsUncompletedTask(taskId2, projectTaskId2, "test", "unit test", null, TeamTaskStatus.NOT_ASSIGNED));
    }

    @Test
    void assignsTask(){
        Team team = Team.createNew(getTeamId(), "project team");
        TeamTaskId taskId = getTaskId();
        TeamMemberId memberId = getMemberId();
        ProjectTaskId projectTaskId = getProjectTaskId();
        Team afterAdd = team.addTask(taskId, projectTaskId, "test", "robot framework");
        afterAdd = afterAdd.addMember(memberId, "john doe", "tester");
        final Team teamWithAssignedTask = afterAdd.assignTask(taskId, memberId);
        assertTrue(teamWithAssignedTask.containsUncompletedTask(taskId, projectTaskId, "test", "robot framework", memberId, TeamTaskStatus.ASSIGNED));
    }

    @Test
    void throwsExceptionWhenTryingToRemoveMemberWhoHasAssignedTask(){
        Team team = Team.createNew(getTeamId(), "project team");
        TeamTaskId taskId = getTaskId();
        TeamTaskId taskId2 = getTaskId();
        TeamMemberId memberId = getMemberId();
        ProjectTaskId projectTaskId = getProjectTaskId();
        ProjectTaskId projectTaskId2 = getProjectTaskId();
        Team afterAdd = team.addTask(taskId, projectTaskId, "test", "robot framework");
        afterAdd = afterAdd.addTask(taskId2, projectTaskId2, "write code", "java code");
        afterAdd = afterAdd.addMember(memberId, "john doe", "tester");
        final Team teamWithAssignedTask = afterAdd.assignTask(taskId, memberId);
        assertTrue(teamWithAssignedTask.containsUncompletedTask(taskId, projectTaskId, "test", "robot framework", memberId, TeamTaskStatus.ASSIGNED));
        assertThrows(TeamMemberHasAssignedTasksException.class, () -> teamWithAssignedTask.removeMember(memberId));
    }

    @Test
    void throwsExceptionWhileAssigningTaskBecauseUnknownMember(){
        Team team = Team.createNew(getTeamId(), "project team");
        TeamTaskId taskId = getTaskId();
        TeamMemberId memberId = getMemberId();
        ProjectTaskId projectTaskId = getProjectTaskId();
        Team afterAdd = team.addTask(taskId, projectTaskId, "test", "robot framework");
        assertThrows(UnknownTeamMemberIdException.class, () -> afterAdd.assignTask(taskId, memberId));
    }

    @Test
    void throwsExceptionWhileAssigningTaskBecauseUnknownTask(){
        Team team = Team.createNew(getTeamId(), "project team");
        TeamTaskId taskId = getTaskId();
        TeamMemberId memberId = getMemberId();
        assertThrows(UnknownTeamTaskIdException.class, () -> team.assignTask(taskId, memberId));
    }

    @Test
    void throwsExceptionWhileAssigningTaskBecauseTaskAlreadyAssigned(){
        Team team = Team.createNew(getTeamId(), "project team");
        TeamTaskId taskId = getTaskId();
        TeamMemberId memberId = getMemberId();
        ProjectTaskId projectTaskId = getProjectTaskId();
        Team afterAdd = team.addTask(taskId, projectTaskId, "test", "robot framework");
        afterAdd = afterAdd.addMember(memberId, "john doe", "tester");
        final Team teamWithAssignedTask = afterAdd.assignTask(taskId, memberId);
        assertTrue(teamWithAssignedTask.containsUncompletedTask(taskId, projectTaskId, "test", "robot framework", memberId, TeamTaskStatus.ASSIGNED));
        assertThrows(TaskTransitionNotAllowedException.class, () -> teamWithAssignedTask.assignTask(taskId, memberId));
    }

    @Test
    void marksTaskInProgress(){
        Team team = Team.createNew(getTeamId(), "project team");
        TeamTaskId taskId = getTaskId();
        TeamMemberId memberId = getMemberId();
        ProjectTaskId projectTaskId = getProjectTaskId();
        Team afterAdd = team.addTask(taskId, projectTaskId, "test", "robot framework");
        afterAdd = afterAdd.addMember(memberId, "john doe", "tester");
        final Team teamWithAssignedTask = afterAdd.assignTask(taskId, memberId);
        assertTrue(teamWithAssignedTask.containsUncompletedTask(taskId, projectTaskId, "test", "robot framework", memberId, TeamTaskStatus.ASSIGNED));
        final Team teamWithTaskInProgress = teamWithAssignedTask.markTaskInProgress(taskId);
        assertTrue(teamWithTaskInProgress.containsUncompletedTask(taskId, projectTaskId, "test", "robot framework", memberId, TeamTaskStatus.IN_PROGRESS));
    }

    @Test
    void throwsExceptionWhenTryingToMarkNotAssignedTaskToInProgress(){
        Team team = Team.createNew(getTeamId(), "project team");
        TeamTaskId taskId = getTaskId();
        TeamMemberId memberId = getMemberId();
        ProjectTaskId projectTaskId = getProjectTaskId();
        Team afterAdd = team.addTask(taskId, projectTaskId, "test", "robot framework");
        final Team teamWithMember = afterAdd.addMember(memberId, "john doe", "tester");
        assertThrows(TaskTransitionNotAllowedException.class, () -> teamWithMember.markTaskInProgress(taskId));
    }

    @Test
    void throwsExceptionWhenTryingToMarkNonExistingTaskToInProgress(){
        Team team = Team.createNew(getTeamId(), "project team");
        TeamTaskId taskId = getTaskId();
        assertThrows(UnknownTeamTaskIdException.class, () -> team.markTaskInProgress(taskId));
    }

    @Test
    void marksTaskCompleted(){
        Team team = Team.createNew(getTeamId(), "project team");
        TeamTaskId taskId = getTaskId();
        TeamMemberId memberId = getMemberId();
        ProjectTaskId projectTaskId = getProjectTaskId();
        Team afterAdd = team.addTask(taskId, projectTaskId, "test", "robot framework");
        afterAdd = afterAdd.addMember(memberId, "john doe", "tester");
        final Team teamWithAssignedTask = afterAdd.assignTask(taskId, memberId);
        final Team teamWithTaskInProgress = teamWithAssignedTask.markTaskInProgress(taskId);
        final Team teamWithCompletedTask = teamWithTaskInProgress.markTaskCompleted(taskId, ActualSpentTime.fromMinutes(100));
        assertTrue(teamWithCompletedTask.containsCompletedTask(taskId, projectTaskId, "test", "robot framework",  ActualSpentTime.fromMinutes(100)));
    }

    @Test
    void throwsExceptionWhenTryingToMarkNotAssignedTaskCompleted(){
        Team team = Team.createNew(getTeamId(), "project team");
        TeamTaskId taskId = getTaskId();
        TeamMemberId memberId = getMemberId();
        ProjectTaskId projectTaskId = getProjectTaskId();
        Team afterAdd = team.addTask(taskId, projectTaskId, "test", "robot framework");
        final Team teamWithMember = afterAdd.addMember(memberId, "john doe", "tester");
        assertThrows(TaskTransitionNotAllowedException.class, () -> teamWithMember.markTaskCompleted(taskId, ActualSpentTime.fromMinutes(10)));

        assertTrue(teamWithMember.containsUncompletedTask(taskId, projectTaskId, "test", "robot framework", null, TeamTaskStatus.NOT_ASSIGNED));
    }

    @Test
    void throwsExceptionWhenTryingToMarkNonExistingTaskCompleted(){
        Team team = Team.createNew(getTeamId(), "project team");
        TeamTaskId taskId = getTaskId();
        assertThrows(UnknownTeamTaskIdException.class, () -> team.markTaskCompleted(taskId, ActualSpentTime.fromMinutes(10)));
    }

    @Test
    void unassignsTask(){
        Team team = Team.createNew(getTeamId(), "project team");
        TeamTaskId taskId = getTaskId();
        TeamMemberId memberId = getMemberId();
        ProjectTaskId projectTaskId = getProjectTaskId();
        Team afterAdd = team.addTask(taskId, projectTaskId, "test", "robot framework");
        afterAdd = afterAdd.addMember(memberId, "john doe", "tester");
        final Team teamWithAssignedTask = afterAdd.assignTask(taskId, memberId);
        final Team teamWithUnassignedTask = teamWithAssignedTask.markTaskUnassigned(taskId);
        assertTrue(teamWithUnassignedTask.containsUncompletedTask(taskId, projectTaskId, "test", "robot framework", null, TeamTaskStatus.NOT_ASSIGNED));
    }

    @Test
    void throwsExceptionWhenTryingToUnassignUnassignableTask(){
        Team team = Team.createNew(getTeamId(), "project team");
        TeamTaskId taskId = getTaskId();
        TeamMemberId memberId = getMemberId();
        ProjectTaskId projectTaskId = getProjectTaskId();
        Team afterAdd = team.addTask(taskId, projectTaskId, "test", "robot framework");
        afterAdd = afterAdd.addMember(memberId, "john doe", "tester");
        final Team teamWithAssignedTask = afterAdd.assignTask(taskId, memberId);
        final Team teamWithUnassignedTask = teamWithAssignedTask.markTaskUnassigned(taskId);
        assertTrue(teamWithUnassignedTask.containsUncompletedTask(taskId, projectTaskId, "test", "robot framework", null, TeamTaskStatus.NOT_ASSIGNED));
        assertThrows(TaskTransitionNotAllowedException.class, () -> teamWithUnassignedTask.markTaskUnassigned(taskId));
    }

    @Test
    void throwsExceptionWhenTryingToMarkNonExistingTaskUnAssigned(){
        Team team = Team.createNew(getTeamId(), "project team");
        TeamTaskId taskId = getTaskId();
        assertThrows(UnknownTeamTaskIdException.class, () -> team.markTaskUnassigned(taskId));
    }

    private TeamId getTeamId(){
        return new TeamId(UUID.randomUUID());
    }

    private TeamTaskId getTaskId(){
        return new TeamTaskId(UUID.randomUUID());
    }

    private ProjectTaskId getProjectTaskId(){
        return new ProjectTaskId(UUID.randomUUID());
    }

    private TeamMemberId getMemberId(){
        return new TeamMemberId(UUID.randomUUID());
    }
}