package t03.domain.service;

import org.springframework.stereotype.Service;
import t03.domain.model.Board;
import t03.domain.model.Cell;

@Service
public class MinimaxService {

    public int findBestMove(Board board) {
        Cell[] cells = board.getCells();
        int bestScore = Integer.MIN_VALUE;
        int bestMove = -1;

        for (int i = 0; i < Board.TOTAL_CELLS; i++) {
            if (cells[i] == Cell.EMPTY) {
                cells[i] = Cell.O;
                int score = minimax(cells, 0, false);
                cells[i] = Cell.EMPTY;

                if (score > bestScore) {
                    bestScore = score;
                    bestMove = i;
                }
            }
        }

        return bestMove;
    }

    private int minimax(Cell[] board, int depth, boolean isMaximizing) {
        Cell result = evaluate(board);

        if (result == Cell.O) return 10 - depth;
        if (result == Cell.X) return depth - 10;
        if (isBoardFull(board)) return 0;

        if (isMaximizing) {
            int bestScore = Integer.MIN_VALUE;
            for (int i = 0; i < board.length; i++) {
                if (board[i] == Cell.EMPTY) {
                    board[i] = Cell.O;
                    int score = minimax(board, depth + 1, false);
                    board[i] = Cell.EMPTY;
                    bestScore = Math.max(score, bestScore);
                }
            }
            return bestScore;
        } else {
            int bestScore = Integer.MAX_VALUE;
            for (int i = 0; i < board.length; i++) {
                if (board[i] == Cell.EMPTY) {
                    board[i] = Cell.X;
                    int score = minimax(board, depth + 1, true);
                    board[i] = Cell.EMPTY;
                    bestScore = Math.min(score, bestScore);
                }
            }
            return bestScore;
        }
    }

    private Cell evaluate(Cell[] board) {
        // Check rows
        for (int i = 0; i < 3; i++) {
            int base = i * 3;
            if (board[base] != Cell.EMPTY &&
                    board[base] == board[base + 1] &&
                    board[base] == board[base + 2]) {
                return board[base];
            }
        }

        // Check columns
        for (int i = 0; i < 3; i++) {
            if (board[i] != Cell.EMPTY &&
                    board[i] == board[i + 3] &&
                    board[i] == board[i + 6]) {
                return board[i];
            }
        }

        // Check diagonals
        if (board[0] != Cell.EMPTY &&
                board[0] == board[4] &&
                board[0] == board[8]) {
            return board[0];
        }

        if (board[2] != Cell.EMPTY &&
                board[2] == board[4] &&
                board[2] == board[6]) {
            return board[2];
        }

        return null;
    }

    private boolean isBoardFull(Cell[] board) {
        for (Cell cell : board) {
            if (cell == Cell.EMPTY) {
                return false;
            }
        }
        return true;
    }

}
