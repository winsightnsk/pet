package t03.domain.model;

public enum Cell {

    X('X'),
    O('O'),
    EMPTY(' ');

    private final char symbol;

    Cell(Character symbol) {
        if (symbol == null) this.symbol = ' ';
        else if (symbol == 'O') this.symbol = 'O';
        else if (symbol == 'X') this.symbol = 'X';
        else this.symbol = ' ';
    }

    public char getSymbol() {
        return symbol;
    }

    public static Cell fromChar(Character c) {
        if (c == null) return EMPTY;
        if (c == 'O') return O;
        if (c == 'X') return X;
        return EMPTY;
    }

    public Cell getOpponent() {
        if (this.equals(EMPTY)) return EMPTY;
        return this == X ? O : X;
    }

    @Override
    public String toString() {
        return String.valueOf(symbol);
    }

}
