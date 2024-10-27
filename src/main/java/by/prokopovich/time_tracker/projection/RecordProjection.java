package by.prokopovich.time_tracker.projection;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class RecordProjection {
    private Long id;
    private String description;
    private LocalDateTime createdAt;
    private String createdBy;
}
