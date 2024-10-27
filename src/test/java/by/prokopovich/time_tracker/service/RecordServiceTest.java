package by.prokopovich.time_tracker.service;

import by.prokopovich.time_tracker.dto.request.CreateRecordRequest;
import by.prokopovich.time_tracker.dto.request.UpdateRecordRequest;
import by.prokopovich.time_tracker.dto.response.RecordResponse;
import by.prokopovich.time_tracker.entity.Record;
import by.prokopovich.time_tracker.entity.Task;
import by.prokopovich.time_tracker.entity.User;
import by.prokopovich.time_tracker.exception.IllegalRecordIdException;
import by.prokopovich.time_tracker.projection.RecordProjection;
import by.prokopovich.time_tracker.repository.RecordRepository;
import by.prokopovich.time_tracker.repository.TaskRepository;
import by.prokopovich.time_tracker.repository.UserRepository;
import by.prokopovich.time_tracker.utils.AuxiliaryElements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecordServiceTest {

    @Mock
    private RecordRepository recordRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private RecordService recordService;

    private User user;
    private Task task;
    private Record record;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setEmail("sofia@mail.ru");
        user.setFirstname("Sofia");
        user.setLastname("Kuznecova");

        task = new Task();
        task.setId(1L);
        task.setExecutor(user);

        record = new Record();
        record.setId(1L);
        record.setWorker(user);
        record.setHours((byte) 3);
    }

    @Test
    void create_success() {
        CreateRecordRequest request = new CreateRecordRequest(1L, "Test description", "26/10/2024 15:00:00", (byte) 2);
        when(userRepository.findByEmail("sofia@mail.ru")).thenReturn(Optional.of(user));
        when(taskRepository.findByIdAndExecutor(1L, user)).thenReturn(Optional.of(task));
        when(recordRepository.save(any(Record.class))).thenReturn(record);

        RecordResponse response = recordService.create(request, "sofia@mail.ru");

        assertNotNull(response);
        assertFalse(task.getRecords().isEmpty());
        assertEquals(1, task.getRecords().size());
    }

    @Test
    void searchAllUserRecords_success() {
        UUID userId = UUID.randomUUID();
        RecordProjection projection = new RecordProjection(
                1L,
                "Test description",
                LocalDateTime.parse("26/10/2024 15:00:00", AuxiliaryElements.DATE_FORMAT),
                "Sofia Kuznecova",
                (byte) 2
        );

        List<RecordProjection> projections = List.of(projection);
        Page<RecordProjection> expectedPage = new PageImpl<>(projections);

        when(recordRepository.findAllUserRecords(eq(userId), any(Pageable.class))).thenReturn(expectedPage);

        Page<RecordProjection> result = recordService.searchAllUserRecords(userId, 0, 5);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test description", result.getContent().get(0).getDescription());
    }

    @Test
    void updateUserRecord_success() {
        Long recordId = 1L;
        UUID userId = UUID.randomUUID();
        UpdateRecordRequest request = new UpdateRecordRequest("Updated description", "26/10/2024 15:00:00", (byte) 3);

        when(recordRepository.updateUserRecord(recordId, userId, request.description(), LocalDateTime.parse(request.createdAt(), AuxiliaryElements.DATE_FORMAT), request.hours()))
                .thenReturn(1);

        String result = recordService.updateUserRecord(recordId, userId, request);

        assertEquals("Запись была успешно обновлена", result);
    }

    @Test
    void deleteUserRecord_shouldThrowIllegalRecordIdException() {
        Long recordId = 1L;
        UUID userId = UUID.randomUUID();
        UpdateRecordRequest request = new UpdateRecordRequest("Updated description", "26/10/2024 15:00:00", (byte) 3);

        when(recordRepository.updateUserRecord(eq(recordId), eq(userId), anyString(), any(), any()))
                .thenReturn(0);

        assertThrows(IllegalRecordIdException.class, () -> recordService.updateUserRecord(recordId, userId, request));
    }

    @Test
    void delete_success() {
        Long recordId = 1L;
        UUID userId = UUID.randomUUID();

        when(recordRepository.deleteByIdAndUserId(recordId, userId)).thenReturn(1);

        String result = recordService.delete(recordId, userId);

        assertEquals("Запись была успешно удалена", result);
    }

    @Test
    void delete_shouldThrowRecordNotFound() {
        Long recordId = 1L;
        UUID userId = UUID.randomUUID();

        when(recordRepository.deleteByIdAndUserId(recordId, userId)).thenReturn(0); // Имитация неудачного удаления

        assertThrows(IllegalRecordIdException.class, () -> recordService.delete(recordId, userId));
    }

}
