package by.prokopovich.time_tracker.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RecordResponse(Long id, String description, LocalDateTime date, String author, Byte hours) {
}
