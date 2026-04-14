package t03.domain.model;

import java.util.Arrays;

public class Board {

    public static final int SIZE = 3;
    public static final int TOTAL_CELLS = SIZE * SIZE;

    private final Cell[] cells;

    public Board() {
        this.cells = new Cell[TOTAL_CELLS];
        Arrays.fill(cells, Cell.EMPTY);
    }
    public Board(Cell[] cells) {
        this.cells = Arrays.copyOf(cells, TOTAL_CELLS);
    }
    public Board(String txt) {
        cells = new Cell[TOTAL_CELLS];
        if (txt.length() < TOTAL_CELLS) throw new IllegalArgumentException("Короткий входной текст доски");
        for (int i = 0; i < TOTAL_CELLS; i++) cells[i] = Cell.fromChar(txt.charAt(i));
    }

    public Cell[] getCells() {
        return Arrays.copyOf(cells, TOTAL_CELLS);
    }
    public Cell getCell(int index) {
        validateIndex(index);
        return cells[index];
    }
    public void setCell(int index, Cell cell) {
        validateIndex(index);
        cells[index] = cell;
    }


    public Board copy() {
        return new Board(cells);
    }

    public boolean isFull() {
        for (Cell cell : cells) if (cell == Cell.EMPTY) return false;
        return true;
    }

    public boolean isEmpty(int index) {
        return getCell(index) == Cell.EMPTY;
    }

    public String toBoardString() {
        StringBuilder sb = new StringBuilder(TOTAL_CELLS);
        for (Cell cell : cells) sb.append(cell.getSymbol());
        return sb.toString();
    }

    private void validateIndex(int index) {
        if (index < 0 || index >= TOTAL_CELLS)
            throw new IllegalArgumentException("Invalid cell index: " + index);
    }

}
