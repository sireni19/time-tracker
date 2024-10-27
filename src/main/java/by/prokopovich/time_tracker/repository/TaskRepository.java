package by.prokopovich.time_tracker.repository;

import by.prokopovich.time_tracker.entity.Task;
import by.prokopovich.time_tracker.entity.User;
import by.prokopovich.time_tracker.projection.TaskDetailsProjection;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {

    Optional<Task> findById(Long aLong);

    Optional<Task> findByIdAndExecutor(Long taskId, User executor);

    @Query("""
        SELECT t.id AS taskId,
               t.description AS taskDescription,
               CONCAT(u.firstname, ' ', u.lastname) AS executorName,
               r.id AS recordId,
               r.description AS recordDescription,
               r.createdAt AS createdAt,
               r.createdBy AS createdBy,
               r.hours AS recordHours
        FROM Task t
        LEFT JOIN t.records r
        LEFT JOIN r.worker u
        WHERE t.id = :taskId
        """)
    List<Object[]> findTaskDetails(@Param("taskId") Long taskId);

    int deleteById(Long id);

}
