package by.prokopovich.time_tracker.dto.response;

import lombok.Builder;

@Builder
public record TaskResponse(Long id, String title, String description, Byte expectedHours) {
}
