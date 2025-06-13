package tn.portfolio.reactive.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import reactor.core.publisher.Mono;
import tn.portfolio.reactive.project.domain.ProjectAlreadyCompletedException;
import tn.portfolio.reactive.project.domain.ProjectTimeEstimationWouldBeExceededException;
import tn.portfolio.reactive.project.domain.UnknownProjectIdException;
import tn.portfolio.reactive.project.domain.UnknownProjectTaskIdException;
import tn.portfolio.reactive.team.domain.*;

@ControllerAdvice
public class ControllerExceptionHandler {

    @ExceptionHandler(UnknownProjectIdException.class)
    public Mono<ResponseEntity<String>> handleUnknownProjectIdException(UnknownProjectIdException ex) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage()));
    }

    @ExceptionHandler(ProjectTimeEstimationWouldBeExceededException.class)
    public Mono<ResponseEntity<String>> handleProjectTimeEstimationWouldBeExceededException(ProjectTimeEstimationWouldBeExceededException ex) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage()));
    }

    @ExceptionHandler(UnknownProjectTaskIdException.class)
    public Mono<ResponseEntity<String>> handleUnknownProjectTaskIdException(UnknownProjectTaskIdException ex) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage()));
    }

    @ExceptionHandler(ProjectAlreadyCompletedException.class)
    public Mono<ResponseEntity<String>> ProjectAlreadyCompletedException(ProjectAlreadyCompletedException ex) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage()));
    }

    @ExceptionHandler(TaskTransitionNotAllowedException.class)
    public Mono<ResponseEntity<String>> handleTaskNotInProgressException(TaskTransitionNotAllowedException ex) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage()));
    }

    @ExceptionHandler(TaskCannotBeDeletedException.class)
    public Mono<ResponseEntity<String>> handleTaskCannotBeDeletedException(TaskCannotBeDeletedException ex) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage()));
    }

    @ExceptionHandler(UnknownTeamIdException.class)
    public Mono<ResponseEntity<String>> handleUnknownTeamIdException(UnknownTeamIdException ex) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage()));
    }

    @ExceptionHandler(UnknownTeamMemberIdException.class)
    public Mono<ResponseEntity<String>> handleUnknownTeamMemberIdException(UnknownTeamMemberIdException ex) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage()));
    }

    @ExceptionHandler(UnknownTeamTaskIdException.class)
    public Mono<ResponseEntity<String>> handleUnknownTeamTaskIdException(UnknownTeamTaskIdException ex) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage()));
    }

    @ExceptionHandler(TaskAlreadyAssignedException.class)
    public Mono<ResponseEntity<String>> handleTaskAlreadyAssignedException(TaskAlreadyAssignedException ex) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ex.getMessage()));
    }

    @ExceptionHandler(TeamMemberHasAssignedTasksException.class)
    public Mono<ResponseEntity<String>> handleTeamMemberHasAssignedTasksException(TeamMemberHasAssignedTasksException ex) {
        return Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage()));
    }
}