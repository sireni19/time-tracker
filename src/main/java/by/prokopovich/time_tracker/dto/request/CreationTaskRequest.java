package by.prokopovich.time_tracker.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record CreationTaskRequest(@NotNull String title,
                                  String description,
                                  Byte expectedHours) {
}
