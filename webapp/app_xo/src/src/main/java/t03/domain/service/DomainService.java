package t03.domain.service;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import t03.datasource.model.GameEntity;
import t03.datasource.model.HumanEntity;
import t03.datasource.repository.GameInterface;
import t03.datasource.mapper.DataMapperComponent;
import t03.datasource.repository.HumanInterface;
import t03.domain.DomainInterface;
import t03.domain.model.*;
import t03.web.mapper.WebMapperComponent;
import t03.web.model.HashHuman;
import t03.web.model.api.DTOGame;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service @AllArgsConstructor
public class DomainService implements DomainInterface {

    private final MinimaxService minimaxService;
    private final GameInterface gameRepository;
    private final HumanInterface humanRepository;
    private final DataMapperComponent dataMapper;
    private final WebMapperComponent webMapper;

    @Override @Transactional
    public void removeGame(UUID gameId) {
        if (gameId != null) gameRepository.deleteById(gameId);
    }
    @Override
    public void removeGame(GameEntity gameEntity) {
        if (gameEntity != null) removeGame(gameEntity.getId());
    }
    @Override
    public void removeGame(HashHuman hashHuman) {
        getGameByHid(hashHuman.getId())
                .ifPresent(game -> removeGame(game.getId()));
    }

    @Override
    public Optional<Game> getGame(UUID gameId) {
        return dataMapper.toDomain(gameRepository.findById(gameId));
    }

    @Override
    public Optional<Game> getGameByHid(UUID humanId) {
        if (humanId == null) return Optional.empty();
        Optional<GameEntity> oge = gameRepository.findByHuman(humanId);
        return oge.map(dataMapper::toDomain);
    }

    private Game createGame(@NonNull UUID humanX, UUID humanO) {
        Game g = new Game(
                UUID.randomUUID(),
                new Board(),
                Cell.EMPTY,
                false,
                humanX,
                String.valueOf(humanRepository.findById(humanX).map(HumanEntity::getName)),
                humanO,
                humanO==null ? null: String.valueOf(humanRepository.findById(humanO).map(HumanEntity::getName)),
                true);
        gameRepository.save(dataMapper.toEntity(g));
        return g;
    }

    @Override
    public Game newGame(@NonNull UUID humanX, UUID humanO) {
        getGameByHid(humanX)
                .ifPresent(game -> gameRepository.deleteById(game.getId()));
        getGameByHid(humanO)
                .ifPresent(game -> gameRepository.deleteById(game.getId()));
        return createGame(humanX, humanO);
    }
    
    @Override
    public Game getOrNewGame(UUID human) {
        return getOrNewGame(human, null);
    }

    @Override @Transactional
    public Game getOrNewGame(UUID humanX, UUID humanO) {
        Optional<Game> game = dataMapper.toDomain(gameRepository.findByHuman(humanX));
        return game.orElse(createGame(humanX,humanO));
    }

    private MoveResult checkGameStatusAfterMove(Game game, Cell currentPlayer, String winMessage) {
        // Проверяем, выиграл ли текущий игрок
        if (checkWinner(game.getBoard()) == currentPlayer) {
            game.setWinner(currentPlayer);
            game.setGameOver(true);
            return MoveResult.valid(game, winMessage);
        }
        // Проверяем ничью
        if (game.getBoard().isFull()) {
            game.setGameOver(true);
            return MoveResult.valid(game, "Ничья! 🤝");
        }
        return null; // Игра продолжается
    }

    @Override
    public MoveResult makeMove(Game game, int position, UUID human) {
        if ((game.isXPlay() && !human.equals(game.getHumanX()))
                || (!game.isXPlay() && human.equals(game.getHumanX())))
            return MoveResult.invalid(game, "Сейчас очередь другого человека");
        if (game.isGameOver())
            return MoveResult.invalid(game, "Игра уже завершена!");
        if (!game.isValidMove(position))
            return MoveResult.invalid(game, "Недопустимый ход!");

        Cell curCell = game.isXPlay() ? Cell.X : Cell.O ;

        // Создаем копию игры для обработки
        Game newGame = game.copy();
        // Ход игрока (X)
        newGame.getBoard().setCell(position, curCell);
        // Проверяем результат после хода игрока
        String name = String.valueOf(game.isXPlay() ? game.getNameX() : game.getNameO() );
        MoveResult playerResult = checkGameStatusAfterMove(newGame, curCell, "Выиграл " + name);
        if (playerResult != null) {
            gameRepository.save(dataMapper.toEntity(playerResult.getGame()));
            return playerResult;
        }

        newGame.setXPlay(!newGame.isXPlay());

        // Ход компьютера
        if (newGame.getHumanO() == null) {
            // Ход компьютера (O)
            int computerMove = minimaxService.findBestMove(newGame.getBoard());
            if (computerMove != -1) {
                newGame.getBoard().setCell(computerMove, Cell.O);
                // Проверяем, выиграл ли компьютер
                MoveResult computerResult = checkGameStatusAfterMove(newGame, Cell.O, "Компьютер выиграл! 🤖");
                if (computerResult != null) {
                    gameRepository.save(dataMapper.toEntity(computerResult.getGame()));
                    return computerResult;
                }
            }
            newGame.setXPlay(true);
        }

        gameRepository.save(dataMapper.toEntity(newGame));
        return MoveResult.valid(newGame, "Ход сделан");
    }

    private Cell checkWinner(Board board) {
        Cell[] cells = board.getCells();

        // Проверка строк
        for (int i = 0; i < 3; i++) {
            int base = i * 3;
            if (cells[base] != Cell.EMPTY &&
                    cells[base] == cells[base + 1] &&
                    cells[base] == cells[base + 2]) {
                return cells[base];
            }
        }

        // Проверка столбцов
        for (int i = 0; i < 3; i++) {
            if (cells[i] != Cell.EMPTY &&
                    cells[i] == cells[i + 3] &&
                    cells[i] == cells[i + 6]) {
                return cells[i];
            }
        }

        // Проверка диагоналей
        if (cells[0] != Cell.EMPTY &&
                cells[0] == cells[4] &&
                cells[0] == cells[8]) {
            return cells[0];
        }

        if (cells[2] != Cell.EMPTY &&
                cells[2] == cells[4] &&
                cells[2] == cells[6]) {
            return cells[2];
        }

        return null;
    }

    @Override
    public List<String> getAllNames() {
        return humanRepository.getAllNames();
    }

    @Override
    public List<HashHuman> getOpponentList(HashHuman human) {
        if (human == null) throw new RuntimeException("human is NULL");
        return humanRepository.findHumansNotInAnyGame().stream()
                .map(webMapper::heTohh)
                .filter(id -> !id.getId().equals(human.getId()))
                .toList();
    }

    @Override
    public Optional<HashHuman> hhFindId(UUID id) {
        if (id == null) return Optional.empty();
        return humanRepository.findById(id).map(webMapper::heTohh);
    }

    @Transactional
    @Override
    public Boolean dropHuman(HashHuman requestBy) {
        try {
            humanRepository.deleteById(requestBy.getId());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Optional<DTOGame> DTOgameByHH(HashHuman hashHuman) {
        if (hashHuman == null) return Optional.empty();
        Optional<GameEntity> oge = gameRepository.findByHuman(hashHuman.getId());
        return oge.map(webMapper::geToDTO);
    }

    @Override
    public HumanEntity heByHH(HashHuman hashHuman) {
        return humanRepository.findById(hashHuman.getId()).orElse(null);
    }

}
