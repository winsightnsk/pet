package t03.datasource.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import t03.datasource.model.SessionEntity;

import java.time.LocalDateTime;
import java.util.UUID;

public interface SessionRepository extends CrudRepository<SessionEntity, UUID> {

    @Modifying
    @Transactional
    @Query("DELETE FROM SessionEntity s WHERE s.expiresAt < :datetime")
    void deleteExpiredSessions(@Param("datetime") LocalDateTime datetime);

}