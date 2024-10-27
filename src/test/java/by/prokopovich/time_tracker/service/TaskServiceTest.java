package by.prokopovich.time_tracker.service;

import by.prokopovich.time_tracker.dto.request.AssignAndReleaseUserRequest;
import by.prokopovich.time_tracker.dto.request.CreationTaskRequest;
import by.prokopovich.time_tracker.dto.response.TaskResponse;
import by.prokopovich.time_tracker.entity.Status;
import by.prokopovich.time_tracker.entity.Task;
import by.prokopovich.time_tracker.entity.User;
import by.prokopovich.time_tracker.projection.TaskDetailsProjection;
import by.prokopovich.time_tracker.repository.TaskRepository;
import by.prokopovich.time_tracker.repository.UserRepository;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TaskService taskService;

    private User user;
    private Task task;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("sofia@mail.ru");
        user.setFirstname("Sofia");
        user.setLastname("Kuznecova");

        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setExpectedHours((byte) 5);
    }

    @Test
    void testCreateTask_success() {
        CreationTaskRequest request = new CreationTaskRequest("Test Task", "Test Description", (byte) 5);

        when(taskRepository.save(any(Task.class))).thenReturn(task);

        TaskResponse response = taskService.create(request);

        assertNotNull(response);
        assertEquals(task.getId(), response.id());
        assertEquals(task.getTitle(), response.title());
        assertEquals(task.getDescription(), response.description());
        assertEquals(task.getExpectedHours(), response.expectedHours());
    }

    @Test
    void testAssignUser_success() {
        AssignAndReleaseUserRequest request = new AssignAndReleaseUserRequest("sofia@mail.ru", 1L);

        when(userRepository.findByEmail("sofia@mail.ru")).thenReturn(Optional.of(user));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        String result = taskService.assignUser(request);

        assertEquals("Задача успешно назначена пользователю sofia@mail.ru", result);
        assertEquals(user, task.getExecutor());
        assertEquals(Status.IN_PROGRESS, task.getStatus());
    }

    @Test
    void testAssignUser_UserAlreadyAssigned() {
        User anotherUser = new User();
        anotherUser.setEmail("another@mail.ru");
        task.setExecutor(anotherUser);

        AssignAndReleaseUserRequest request = new AssignAndReleaseUserRequest("sofia@mail.ru", 1L);

        when(userRepository.findByEmail("sofia@mail.ru")).thenReturn(Optional.of(user));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        String result = taskService.assignUser(request);

        assertEquals("Над задачей уже работает другой пользователь: another@mail.ru", result);
    }

    @Test
    void testReleaseUser() {
        task.setExecutor(user);
        AssignAndReleaseUserRequest request = new AssignAndReleaseUserRequest("sofia@mail.ru", 1L);

        when(userRepository.findByEmail("sofia@mail.ru")).thenReturn(Optional.of(user));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        String result = taskService.releaseUser(request);

        assertEquals("Задача успешно снята с пользователя sofia@mail.ru", result);
        assertNull(task.getExecutor());
    }

    @Test
    void testReleaseUser_UserNotAssigned() {
        AssignAndReleaseUserRequest request = new AssignAndReleaseUserRequest("sofia@mail.ru", 1L);

        when(userRepository.findByEmail("sofia@mail.ru")).thenReturn(Optional.of(user));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        assertThrows(BadRequestException.class, () -> taskService.releaseUser(request));
    }

    @Test
    void deleteTask_success() {
        when(taskRepository.deleteById(1L)).thenReturn(1);

        String result = taskService.delete(1L);

        assertEquals("Задача удалена", result);
    }

    @Test
    void deleteTask_NotFound() {
        when(taskRepository.deleteById(1L)).thenReturn(0);

        String result = taskService.delete(1L);

        assertEquals("Не удалось удалить задачу", result);
    }

    @Test
    void findTaskInfoById_WithResults() {
        Long taskId = 1L;
        List<Object[]> results = Arrays.asList(new Object[][]{
                {taskId, "Test Task Description", "Sofia Kuznecova", null, null, LocalDateTime.now(), "Sofia", (byte) 2}
        });

        when(taskRepository.findTaskDetails(taskId)).thenReturn(results);

        TaskDetailsProjection result = taskService.findTaskInfoById(taskId);

        assertNotNull(result);
        assertEquals(taskId, result.getTaskId());
        assertEquals("Test Task Description", result.getTaskDescription());
        assertEquals("Sofia Kuznecova", result.getExecutorName());
    }

    @Test
    void findTaskInfoById_NoResults() {
        when(taskRepository.findTaskDetails(any())).thenReturn(Collections.emptyList());

        TaskDetailsProjection result = taskService.findTaskInfoById(any());

        assertNull(result);
    }
}
