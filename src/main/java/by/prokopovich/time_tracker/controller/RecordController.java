package by.prokopovich.time_tracker.controller;

import by.prokopovich.time_tracker.dto.request.CreateRecordRequest;
import by.prokopovich.time_tracker.dto.request.UpdateRecordRequest;
import by.prokopovich.time_tracker.dto.response.RecordResponse;
import by.prokopovich.time_tracker.entity.User;
import by.prokopovich.time_tracker.projection.RecordProjection;
import by.prokopovich.time_tracker.service.RecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/records")
@Slf4j
public class RecordController {

    private final RecordService recordService;

    @PostMapping("/create")
    public ResponseEntity<RecordResponse> create(@Valid @RequestBody CreateRecordRequest request) {
        log.info("Вызов метода create() в контроллере RecordController ");
        // Получаем текущего авторизованного пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return ResponseEntity.status(HttpStatus.CREATED).body(recordService.create(request, username));
    }

    @GetMapping("/search-all")
    public ResponseEntity<Page<RecordProjection>> getAllRecords(@RequestParam(defaultValue = "0") Integer page,
                                                                @RequestParam(required = false,defaultValue = "5")Integer limit) {
        log.info("Вызов метода getAllRecords() в контроллере RecordController ");
        // Авторизованный пользователь получает только свои записи
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            // Используем сопоставление с образцом
            return ResponseEntity.status(HttpStatus.OK).body(recordService.searchAllUserRecords(user.getId(),page,limit));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PatchMapping("/update/{recordId}")
    public ResponseEntity<String> update(@PathVariable(name = "recordId") Long recordId,
                                                @Valid @RequestBody UpdateRecordRequest request) {
        log.info("Вызов метода update() в контроллере RecordController ");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return ResponseEntity.status(HttpStatus.OK).body(recordService.updateUserRecord(recordId, user.getId(), request));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping("/delete/{recordId}")
    public ResponseEntity<String> delete(@PathVariable(name = "recordId") Long recordId) {
        log.info("Вызов метода delete() в контроллере RecordController ");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return ResponseEntity.status(HttpStatus.OK).body(recordService.delete(recordId, user.getId()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
