package by.prokopovich.time_tracker.controller;

import by.prokopovich.time_tracker.dto.request.CreateOrUpdateRecordRequest;
import by.prokopovich.time_tracker.dto.response.RecordResponse;
import by.prokopovich.time_tracker.entity.User;
import by.prokopovich.time_tracker.projection.RecordProjection;
import by.prokopovich.time_tracker.service.RecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/records")
public class RecordController {

    private final RecordService recordService;

    @PostMapping("/create")
    public ResponseEntity<RecordResponse> createRecord(@RequestBody CreateOrUpdateRecordRequest request) {
        // Получаем текущего авторизованного пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(recordService.create(request, username));
    }

    @GetMapping("/search-all")
    public ResponseEntity<List<RecordProjection>> getAllRecords() {
        // Авторизованный пользователь получает только свои записи
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            // Используем сопоставление с образцом
            return ResponseEntity.status(HttpStatus.OK).body(recordService.searchAllUserRecords(user.getId()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PatchMapping("/update/{recordId}")
    public ResponseEntity<String> updateRecord(@PathVariable(name = "recordId") Long recordId,
                                               @RequestBody CreateOrUpdateRecordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return ResponseEntity.status(HttpStatus.OK).body(recordService.updateUserRecord(recordId, user.getId(), request));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping("/delete/{recordId}")
    public ResponseEntity<String> deleteRecord(@PathVariable(name = "recordId") Long recordId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return ResponseEntity.status(HttpStatus.OK).body(recordService.delete(recordId, user.getId()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
