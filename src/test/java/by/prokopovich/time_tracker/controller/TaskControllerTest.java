package by.prokopovich.time_tracker.controller;

import by.prokopovich.time_tracker.controller.handler.GlobalExceptionHandler;
import by.prokopovich.time_tracker.dto.request.AssignAndReleaseUserRequest;
import by.prokopovich.time_tracker.dto.request.CreationTaskRequest;
import by.prokopovich.time_tracker.dto.response.TaskResponse;
import by.prokopovich.time_tracker.projection.TaskDetailsProjection;
import by.prokopovich.time_tracker.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(taskController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void сreateTask() throws Exception {
        CreationTaskRequest request = new CreationTaskRequest("Test Task", "Test Description", (byte) 5);
        TaskResponse response = new TaskResponse(1L, "Test Task", "Test Description", (byte) 5);

        when(taskService.create(any(CreationTaskRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/tasks/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.id()))
                .andExpect(jsonPath("$.title").value(response.title()))
                .andExpect(jsonPath("$.description").value(response.description()));

    }

    @Test
    void assignUserToTask() throws Exception {
        AssignAndReleaseUserRequest request = new AssignAndReleaseUserRequest("sofia@mail.ru", 1L);
        String responseMessage = "Задача успешно назначена пользователю sofia@mail.ru";

        when(taskService.assignUser(any(AssignAndReleaseUserRequest.class))).thenReturn(responseMessage);

        mockMvc.perform(patch("/api/v1/tasks/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void releaseUserFromTask() throws Exception {
        AssignAndReleaseUserRequest request = new AssignAndReleaseUserRequest("sofia@mail.ru", 1L);
        String responseMessage = "Задача успешно снята с пользователя sofia@mail.ru";

        when(taskService.releaseUser(any(AssignAndReleaseUserRequest.class))).thenReturn(responseMessage);

        mockMvc.perform(patch("/api/v1/tasks/release")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void showTask() throws Exception {
        Long taskId = 1L;
        TaskDetailsProjection projection =
                new TaskDetailsProjection(taskId, "Test Task", "Sofia Kuznecova", new ArrayList<>());

        when(taskService.findTaskInfoById(taskId)).thenReturn(projection);

        mockMvc.perform(get("/api/v1/tasks/show/{taskId}", taskId))
                .andExpect(status().isOk());
    }

    @Test
    void deleteTask() throws Exception {
        Long taskId = 1L;
        String responseMessage = "Задача удалена";

        when(taskService.delete(taskId)).thenReturn(responseMessage);

        mockMvc.perform(delete("/api/v1/tasks/delete/{taskId}", taskId))
                .andExpect(status().isOk());
    }
}
