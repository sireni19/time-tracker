package by.prokopovich.time_tracker.projection;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TaskDetailsProjection {
    private Long taskId;
    private String taskDescription;
    private String executorName;
    private List<RecordProjection> records;
}
