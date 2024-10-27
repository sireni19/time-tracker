package by.prokopovich.time_tracker.service;

import by.prokopovich.time_tracker.dto.request.AssignAndReleaseUserRequest;
import by.prokopovich.time_tracker.dto.request.CreationTaskRequest;
import by.prokopovich.time_tracker.dto.response.TaskResponse;
import by.prokopovich.time_tracker.entity.Status;
import by.prokopovich.time_tracker.entity.Task;
import by.prokopovich.time_tracker.entity.User;
import by.prokopovich.time_tracker.exception.TaskNotFoundException;
import by.prokopovich.time_tracker.exception.UserNotFoundException;
import by.prokopovich.time_tracker.projection.RecordProjection;
import by.prokopovich.time_tracker.projection.TaskDetailsProjection;
import by.prokopovich.time_tracker.repository.TaskRepository;
import by.prokopovich.time_tracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskResponse create(CreationTaskRequest request) {
        log.info("Вызов метода create() в сервисе TaskService");
        Task savedTask = taskRepository.save(new Task(request.title(), request.description(), request.expectedHours()));
        return TaskResponse.builder()
                .id(savedTask.getId())
                .title(savedTask.getTitle())
                .description(savedTask.getDescription())
                .expectedHours(savedTask.getExpectedHours())
                .build();
    }

    @Transactional
    public String assignUser(AssignAndReleaseUserRequest request) {
        log.info("Вызов метода assignUser() в сервисе TaskService");
        User user = userRepository.findByEmail(request.username())
                .orElseThrow(() -> new UserNotFoundException(String.format("Пользователя %s нет", request.username())));

        Task task = taskRepository.findById(request.taskId())
                .orElseThrow(() -> new TaskNotFoundException(String.format("Задачи с номером %s нет", request.taskId())));

        if (task.getExecutor() == null) {
            task.setExecutor(user);
            task.setStatus(Status.IN_PROGRESS);
            taskRepository.save(task);
            return "Задача успешно назначена пользователю " + request.username();
        } else {
            return "Над задачей уже работает другой пользователь: " + task.getExecutor().getUsername();
        }
    }

    @SneakyThrows
    @Transactional
    public String releaseUser(AssignAndReleaseUserRequest request) {
        log.info("Вызов метода releaseUser() в сервисе TaskService");
        User user = userRepository.findByEmail(request.username())
                .orElseThrow(() -> new UserNotFoundException(String.format("Пользователя %s нет", request.username())));

        Task task = taskRepository.findById(request.taskId())
                .orElseThrow(() -> new TaskNotFoundException(String.format("Задачи с номером %s нет", request.taskId())));
        //проверка случая, если у задачи не было исполнителя или исполнитель отличный от запрашиваемого
        if (task.getExecutor() != null && task.getExecutor().getId().equals(user.getId())) {
            task.setExecutor(null);
            log.info("Задача с ID {} успешно снята с пользователя {}", task.getId(), request.username());
            return "Задача успешно снята с пользователя " + request.username();
        }
        log.warn("Невозможно снять задачу с пользователя {}: задача не назначена этому пользователю", request.username());
        throw new BadRequestException("Проверьте пользователя или задачу");
    }

    @Transactional
    public TaskDetailsProjection findTaskInfoById(Long taskId) {
        List<Object[]> results = taskRepository.findTaskDetails(taskId);

        // Получаем первую запись для основной информации о задаче
        TaskDetailsProjection taskDetails = results.stream()
                .findFirst()
                .map(row -> new TaskDetailsProjection(
                        (Long) row[0], // taskId
                        (String) row[1], // taskDescription
                        (String) row[2], // executorName
                        new ArrayList<>()
                )).orElse(null); // Если нет результатов, возвращаем null

        // Заполняем записи
        List<RecordProjection> records = results.stream()
                .map(row -> {
                    // Получаем LocalDateTime напрямую
                    LocalDateTime createdAt = (LocalDateTime) row[5];
                    return new RecordProjection(
                            (Long) row[3], // recordId
                            (String) row[4], // recordDescription
                            createdAt, // formatted createdAt as String
                            (String) row[6], // createdBy
                            (Byte) row[7] // hours
                    );
                })
                .toList();

        // Добавляем записи к задаче
        if (taskDetails != null) {
            taskDetails.setRecords(records);
        }
        return taskDetails;
    }

    @Transactional
    public String delete(Long taskId) {
        int i = taskRepository.deleteById(taskId);
        if (i == 1) {
            return "Задача удалена";
        }else {
            return "Не удалось удалить задачу";
        }
    }

}
