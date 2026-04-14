package t03.datasource.mapper;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import t03.datasource.model.GameEntity;
import t03.datasource.model.HumanEntity;
import t03.datasource.repository.HumanInterface;
import t03.domain.model.Board;
import t03.domain.model.Game;
import t03.domain.model.Cell;

@Component
public class DataMapperComponent {

    @Autowired
    private HumanInterface humanRepository;

    public GameEntity toEntity(Game game) {
        if (game == null) return null;
        GameEntity entity = new GameEntity();
        entity.setId(game.getId());
        entity.setBoardstate(game.getBoard().toBoardString());
        entity.setWinner(game.getWinner().getSymbol());
        entity.setXplay(game.isXPlay());
        if (game.getHumanX() == null) throw new RuntimeException("game.getHumanX() == null");
        entity.setHumanX(humanRepository.findById(game.getHumanX()).orElse(null));
        if (entity.getHumanX() == null) throw new RuntimeException("findById(game.getHumanX()) == null");
        if (game.getHumanO() != null) {
            HumanEntity playerO = humanRepository.findById(game.getHumanO()).orElse(null);
            entity.setHumanO(playerO);
        } else entity.setHumanO(null);
        return entity;
    }

    public Game toDomain(GameEntity entity) {
        if (entity == null) return null;
        return new Game(
                entity.getId(),
                new Board(entity.getBoardstate()),
                Cell.fromChar(entity.getWinner()),
                !entity.getWinner().equals(Cell.EMPTY.getSymbol()),
                entity.getHumanX().getId(),
                entity.getHumanX().getName(),
                entity.getHumanO() != null ? entity.getHumanO().getId() : null,
                entity.getHumanO() != null ? entity.getHumanO().getName() : null,
                entity.isXplay()
        );
    }
    public Optional<Game> toDomain(Optional<GameEntity> optGE) {
        return optGE.map(this::toDomain);
    }

}
