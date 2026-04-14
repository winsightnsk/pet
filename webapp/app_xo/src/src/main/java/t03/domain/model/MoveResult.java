package t03.domain.model;

public class MoveResult {

    private final Game game;
    private final String message;
    private final boolean validMove;

    public MoveResult(Game game, String message, boolean validMove) {
        this.game = game;
        this.message = message;
        this.validMove = validMove;
    }

    public Game getGame() {
        return game;
    }

    public String getMessage() {
        return message;
    }

    public boolean isValidMove() {
        return validMove;
    }

    public static MoveResult valid(Game game, String message) {
        return new MoveResult(game, message, true);
    }
    public static MoveResult invalid(Game game, String message) {
        return new MoveResult(game, message, false);
    }

}

