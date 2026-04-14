package t03.datasource.repository;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import t03.datasource.model.GameEntity;

import java.util.Optional;
import java.util.UUID;

public interface GameInterface extends CrudRepository<GameEntity, UUID> {

    @Query("SELECT g FROM GameEntity g WHERE g.humanX.id = :searchfor OR g.humanO.id = :searchfor")
    Optional<GameEntity> findByHuman(@Param("searchfor") UUID humanId);

}


