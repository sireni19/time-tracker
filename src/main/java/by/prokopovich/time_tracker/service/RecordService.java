package by.prokopovich.time_tracker.service;

import by.prokopovich.time_tracker.dto.request.CreateOrUpdateRecordRequest;
import by.prokopovich.time_tracker.dto.response.RecordResponse;
import by.prokopovich.time_tracker.entity.Record;
import by.prokopovich.time_tracker.entity.User;
import by.prokopovich.time_tracker.exception.IllegalRecordIdException;
import by.prokopovich.time_tracker.projection.RecordProjection;
import by.prokopovich.time_tracker.repository.RecordRepository;
import by.prokopovich.time_tracker.repository.UserRepository;
import by.prokopovich.time_tracker.utils.AuxiliaryElements;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecordService {

    private final RecordRepository recordRepository;
    private final UserRepository userRepository;

    public RecordResponse create(CreateOrUpdateRecordRequest request, String username) {
        //Т.к. пользователь вошел в систему, он всегда есть
        User user = userRepository.findByEmail(username).get();
        StringBuilder fullName = new StringBuilder(user.getFirstname());
        fullName.append(" ").append(user.getLastname()).append("(").append(username).append(")");

        Record savedRecord = recordRepository.save(
                new Record(request.description(), user, LocalDateTime.parse(request.createdAt(), AuxiliaryElements.DATE_FORMAT), fullName.toString()));

        return RecordResponse.builder()
                .id(savedRecord.getId())
                .description(savedRecord.getDescription())
                .date(savedRecord.getCreatedAt())
                .author(savedRecord.getCreatedBy())
                .build();
    }

    public List<RecordProjection> searchAllUserRecords(UUID id) {
        // Поиск идет по id аутентифицированного пользователя
        return recordRepository.findAllUserRecords(id);
    }

    @Transactional
    public String updateUserRecord(Long recordId, UUID userId, CreateOrUpdateRecordRequest request) {
        int updatedRows = recordRepository
                .updateUserRecord(recordId, userId, request.description(), LocalDateTime.parse(request.createdAt(), AuxiliaryElements.DATE_FORMAT));
        if (updatedRows == 1) {
            return "Запись была успешно обновлена";
        } else {
            throw new IllegalRecordIdException();
        }
    }

    @Transactional
    public String delete(Long recordId, UUID userId) {
        int deletedRows = recordRepository.deleteByIdAndUserId(recordId, userId);
        if (deletedRows == 1) {
            return "Запись была успешно удалена";
        } else {
            throw new IllegalRecordIdException();
        }
    }
}
