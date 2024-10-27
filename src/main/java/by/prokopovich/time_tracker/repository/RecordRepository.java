package by.prokopovich.time_tracker.repository;

import by.prokopovich.time_tracker.entity.Record;
import by.prokopovich.time_tracker.projection.RecordProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecordRepository extends JpaRepository<Record, Long> {

    @Query("""
            SELECT new by.prokopovich.time_tracker.projection.RecordProjection(r.id, r.description, r.createdAt, r.createdBy, r.hours) 
            FROM Record r 
            WHERE r.worker.id = :userId
            """)
    List<RecordProjection> findAllUserRecords(@Param("userId") UUID userId);

    /*
    За счет аннотации @Modifying выполняется один запрос сразу к базе данных, минуя кеш 1-го уровня
     */
    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Record r SET r.description = :description, r.createdAt = :createdAt, r.hours = :hours 
            WHERE r.id = :recordId AND r.worker.id = :userId""")
    int updateUserRecord(@Param("recordId") Long recordId,
                         @Param("userId") UUID userId,
                         @Param("description") String description,
                         @Param("createdAt") LocalDateTime createdAt,
                         @Param("hours") Byte hours);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM Record r WHERE r.id = :recordId AND r.worker.id = :userId")
    int deleteByIdAndUserId(@Param("recordId") Long recordId, @Param("userId") UUID userId);

}



