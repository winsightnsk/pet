package t03.domain;

import lombok.NonNull;
import org.springframework.transaction.annotation.Transactional;
import t03.datasource.model.GameEntity;
import t03.datasource.model.HumanEntity;
import t03.domain.model.Game;
import t03.domain.model.MoveResult;
import t03.web.model.HashHuman;
import t03.web.model.api.DTOGame;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DomainInterface {

    void removeGame(UUID gameId);
    void removeGame(GameEntity gameEntity);

    void removeGame(HashHuman hashHuman);

    Optional<Game> getGame(UUID gameId);

    Optional<Game> getGameByHid(UUID humanId);

    Game newGame(@NonNull UUID humanX, UUID humanO);

    @Transactional
    Game getOrNewGame(UUID human);
    @Transactional
    Game getOrNewGame(UUID humanX, UUID humanO);

    MoveResult makeMove(Game game, int position, UUID human);

    List<String> getAllNames();

    List<HashHuman> getOpponentList(HashHuman human);

    Optional<HashHuman> hhFindId(UUID id);

    @Transactional
    Boolean dropHuman(HashHuman requestBy);

    Optional<DTOGame> DTOgameByHH(HashHuman hashHuman);

    HumanEntity heByHH(HashHuman hashHuman);
}
