package by.prokopovich.time_tracker.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateRecordRequest(@NotNull Long taskId,
                                          @NotNull String description,
                                          @NotNull @Pattern(regexp = "^\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2}$",
                                                  message = "Дата должна быть в формате dd/MM/yyyy HH:mm:ss")
                                          String createdAt,
                                          @NotNull Byte hours) {
}
