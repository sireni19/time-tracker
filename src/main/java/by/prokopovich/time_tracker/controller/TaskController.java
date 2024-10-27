package by.prokopovich.time_tracker.controller;

import by.prokopovich.time_tracker.dto.request.AssignAndReleaseUserRequest;
import by.prokopovich.time_tracker.dto.request.CreationTaskRequest;
import by.prokopovich.time_tracker.dto.response.TaskResponse;
import by.prokopovich.time_tracker.projection.TaskDetailsProjection;
import by.prokopovich.time_tracker.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Slf4j
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/create")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody CreationTaskRequest request) {
        log.info("Вызов метода createTask() в контроллере TaskController");
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.create(request));
    }

    @PatchMapping("/assign")
    public ResponseEntity<String> assignUserToTask(@Valid @RequestBody AssignAndReleaseUserRequest request){
        log.info("Вызов метода assignUserToTask() в контроллере TaskController");
        return ResponseEntity.status(HttpStatus.OK).body(taskService.assignUser(request));
    }

    @PatchMapping("/release")
    public ResponseEntity<String> releaseUserFromTask(@Valid @RequestBody AssignAndReleaseUserRequest request){
        log.info("Вызов метода releaseUserFromTask() в контроллере TaskController");
        return ResponseEntity.status(HttpStatus.OK).body(taskService.releaseUser(request));
    }

    @GetMapping("/show/{taskId}")
    public ResponseEntity<TaskDetailsProjection> showTask(@PathVariable Long taskId){
        log.info("Вызов метода showTask() в контроллере TaskController");
        return ResponseEntity.status(HttpStatus.OK).body(taskService.findTaskInfoById(taskId));
    }

    @DeleteMapping("/delete/{taskId}")
    public ResponseEntity<String> delete(@PathVariable Long taskId){
        log.info("Вызов метода delete() в контроллере TaskController");
        return ResponseEntity.status(HttpStatus.OK).body(taskService.delete(taskId));
    }
}
