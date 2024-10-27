package by.prokopovich.time_tracker.service;

import by.prokopovich.time_tracker.dto.request.CreateRecordRequest;
import by.prokopovich.time_tracker.dto.request.UpdateRecordRequest;
import by.prokopovich.time_tracker.dto.response.RecordResponse;
import by.prokopovich.time_tracker.entity.Record;
import by.prokopovich.time_tracker.entity.Task;
import by.prokopovich.time_tracker.entity.User;
import by.prokopovich.time_tracker.exception.IllegalRecordIdException;
import by.prokopovich.time_tracker.exception.TaskNotFoundException;
import by.prokopovich.time_tracker.projection.RecordProjection;
import by.prokopovich.time_tracker.repository.RecordRepository;
import by.prokopovich.time_tracker.repository.TaskRepository;
import by.prokopovich.time_tracker.repository.UserRepository;
import by.prokopovich.time_tracker.utils.AuxiliaryElements;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecordService {

    private final RecordRepository recordRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Transactional
    public RecordResponse create(CreateRecordRequest request, String username) {
        log.info("Вызов метода create() в сервисе RecordService ");
        //Т.к. пользователь вошел в систему, он всегда есть
        User user = userRepository.findByEmail(username).get();

        //Найдется задача, которая принадлежит пользователю, в которую трекается работа
        Task task = taskRepository.findByIdAndExecutor(request.taskId(), user)
                .orElseThrow(() -> new TaskNotFoundException(String.format("Задачи под номером %s нет или она не принадлежит вам", request.taskId())));

        String fullName = String.format("%s %s (%s)", user.getFirstname(), user.getLastname(), username);

        Record savedRecord = recordRepository.save(
                new Record(
                        request.description(),
                        user,
                        LocalDateTime.parse(request.createdAt(), AuxiliaryElements.DATE_FORMAT),
                        fullName,
                        request.hours()));
        task.setActualHours((byte) (task.getActualHours() + savedRecord.getHours()));
        task.getRecords().add(savedRecord);

        return RecordResponse.builder()
                .id(savedRecord.getId())
                .description(savedRecord.getDescription())
                .date(savedRecord.getCreatedAt())
                .author(savedRecord.getCreatedBy())
                .hours(savedRecord.getHours())
                .build();
    }

    public Page<RecordProjection> searchAllUserRecords(UUID userId, Integer page, Integer limit) {
        log.info("Вызов метода searchAllUserRecords() в сервисе RecordService ");
        // Поиск идет по id аутентифицированного пользователя
        Pageable pageable = PageRequest.of(page, limit, Sort.by(Sort.Order.desc("createdAt")));
        return recordRepository.findAllUserRecords(userId,  pageable);
    }

    @Transactional
    public String updateUserRecord(Long recordId, UUID userId, UpdateRecordRequest request) {
        log.info("Вызов метода updateUserRecord() в сервисе RecordService ");
        int updatedRows = recordRepository
                .updateUserRecord(recordId,
                        userId,
                        request.description(),
                        LocalDateTime.parse(request.createdAt(), AuxiliaryElements.DATE_FORMAT),
                        request.hours());
        if (updatedRows == 1) {
            return "Запись была успешно обновлена";
        } else {
            throw new IllegalRecordIdException();
        }
    }

    @Transactional
    public String delete(Long recordId, UUID userId) {
        log.info("Вызов метода delete() в сервисе RecordService ");
        int deletedRows = recordRepository.deleteByIdAndUserId(recordId, userId);
        if (deletedRows == 1) {
            return "Запись была успешно удалена";
        } else {
            throw new IllegalRecordIdException();
        }
    }
}
