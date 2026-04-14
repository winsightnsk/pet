package t03.datasource.repository;

import org.springframework.data.jpa.repository.Query;
import t03.datasource.model.HumanEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface HumanInterface extends CrudRepository<HumanEntity, UUID> {

    @Query("SELECT name FROM HumanEntity ORDER BY name")
    List<String> getAllNames();
    Optional<HumanEntity> findByName(String name);

    @Query("""
            SELECT h
            FROM HumanEntity h
            WHERE
                h NOT IN (
                    SELECT g.humanX
                    FROM GameEntity g
                )
            AND
                h NOT IN (
                    SELECT g.humanO
                    FROM GameEntity g
                    WHERE g.humanO IS NOT NULL
                )
    """)
    List<HumanEntity> findHumansNotInAnyGame();

}
