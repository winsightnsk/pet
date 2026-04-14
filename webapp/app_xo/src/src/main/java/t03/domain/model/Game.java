package t03.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data @AllArgsConstructor
public class Game {

    private UUID id;
    private Board board;
    private Cell winner;
    private boolean gameOver;
    private UUID humanX;
    private String nameX;
    private UUID humanO;
    private String nameO;
    private boolean xPlay;

    public boolean isValidMove(int index) {
        return !gameOver && index >= 0 && index < Board.TOTAL_CELLS &&
                board.isEmpty(index);
    }

    public Game copy() {
        return new Game(
                id != null ? UUID.fromString(id.toString()) : null,
                board.copy(),
                winner,
                gameOver,
                humanX != null ? UUID.fromString(humanX.toString()) : null,
                nameX,
                humanO != null ? UUID.fromString(humanO.toString()) : null,
                nameO,
                xPlay
        );
    }

}
