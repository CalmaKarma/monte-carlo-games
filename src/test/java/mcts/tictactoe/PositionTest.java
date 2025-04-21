package mcts.tictactoe;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

public class PositionTest {

    @Test
    public void testMove_2_consecutivePlayer() {
        // same‑player twice in a row
        String grid = "X . .\n" +
                ". O .\n" +
                ". . X";
        Position target = Position.parsePosition(grid, 1 /* last player was X */);
        assertThrows(RuntimeException.class,
                () -> target.move(1, 0, 0),
                "Moving twice with the same player should throw"
        );
    }

    @Test
    public void testMove_1_occupied() {
        // cell (0,0) already occupied
        String grid = "X X O\n" +
                "X O O\n" +
                "X X O";
        Position target = Position.parsePosition(grid, 1 /* last player was X */);
        assertThrows(RuntimeException.class,
                () -> target.move(1, 0, 0),
                "Moving into an occupied cell should throw"
        );
    }

    @Test
    public void testMove0_fullBoard() {
        // full board should throw on any move
        String grid = "X O X\n" +
                "O X O\n" +
                "X O X";
        Position target = Position.parsePosition(grid, 1 /* last player was X */);
        assertThrows(RuntimeException.class,
                () -> target.move(0, 0, 1),
                "Moving on a full board should throw"
        );
    }

    @Test
    public void testMove1() {
        String grid = "X . .\n. O .\n. . X";
        Position target = Position.parsePosition(grid, 1);
        Position moved = target.move(0, 0, 1);
        Position expected = Position.parsePosition(grid.replaceFirst("\\.", "O"), 0);
        assertEquals(expected, moved);
    }

    @Test
    public void testMoves() {
        String grid = "X . .\n. O .\n. . X";
        Position target = Position.parsePosition(grid, 1);
        List<int[]> moves = target.moves(0);
        assertEquals(6, moves.size());
        assertArrayEquals(new int[]{0, 1}, moves.get(0));
        assertArrayEquals(new int[]{0, 2}, moves.get(1));
        assertArrayEquals(new int[]{1, 0}, moves.get(2));
        assertArrayEquals(new int[]{1, 2}, moves.get(3));
        assertArrayEquals(new int[]{2, 0}, moves.get(4));
        assertArrayEquals(new int[]{2, 1}, moves.get(5));
    }

    @Test
    public void testWinner0() {
        String grid = "X . .\n. O .\n. . X";
        Position target = Position.parsePosition(grid, 1);
        assertTrue(target.winner().isEmpty());
    }

    @Test
    public void testWinner1() {
        String grid = "X . 0\nX O .\nX . 0";
        Position target = Position.parsePosition(grid, 1);
        Optional<Integer> winner = target.winner();
        assertTrue(winner.isPresent());
        assertEquals(Integer.valueOf(1), winner.get());
    }

    @Test
    public void testWinner2() {
        String grid = "0 . X\n0 X .\nO . X";
        Position target = Position.parsePosition(grid, 0);
        Optional<Integer> winner = target.winner();
        assertTrue(winner.isPresent());
        assertEquals(Integer.valueOf(0), winner.get());
    }

    @Test
    public void testProjectRow() {
        String grid = "X . 0\nX O .\nX . 0";
        Position target = Position.parsePosition(grid, 1);
        assertArrayEquals(new int[]{1, -1, 0}, target.projectRow(0));
        assertArrayEquals(new int[]{1, 0, -1}, target.projectRow(1));
        assertArrayEquals(new int[]{1, -1, 0}, target.projectRow(2));
    }

    @Test
    public void testProjectCol() {
        String grid = "X . 0\nX O .\nX . 0";
        Position target = Position.parsePosition(grid, 1);
        assertArrayEquals(new int[]{1, 1, 1}, target.projectCol(0));
        assertArrayEquals(new int[]{-1, 0, -1}, target.projectCol(1));
        assertArrayEquals(new int[]{0, -1, 0}, target.projectCol(2));
    }

    @Test
    public void testProjectDiag() {
        String grid = "X . 0\nX O .\nX . 0";
        Position target = Position.parsePosition(grid, 1);
        assertArrayEquals(new int[]{1, 0, 0}, target.projectDiag(true));
        assertArrayEquals(new int[]{1, 0, 0}, target.projectDiag(false));
    }

    @Test
    public void testParseCell() {
        assertEquals(0, Position.parseCell("0"));
        assertEquals(0, Position.parseCell("O"));
        assertEquals(0, Position.parseCell("o"));
        assertEquals(1, Position.parseCell("X"));
        assertEquals(1, Position.parseCell("x"));
        assertEquals(1, Position.parseCell("1"));
        assertEquals(-1, Position.parseCell("."));
        assertEquals(-1, Position.parseCell("a"));
    }

    @Test
    public void testThreeInARow() {
        String grid = "X . 0\nX O .\nX . 0";
        Position target = Position.parsePosition(grid, 1);
        assertTrue(target.threeInARow());
    }

    @Test
    public void testFull() {
        assertFalse(Position.parsePosition("X . 0\nX O .\nX . 0", 1).full());
        assertTrue(Position.parsePosition("X X 0\nX O 0\nX X 0", 1).full());
    }

    @Test
    public void testRender() {
        String grid = "X . .\n. O .\n. . X";
        Position target = Position.parsePosition(grid, 1);
        assertEquals(grid, target.render());
    }

    @Test
    public void testToString() {
        Position target = Position.parsePosition("X . .\n. O .\n. . X", 1);
        assertEquals("1,-1,-1\n-1,0,-1\n-1,-1,1", target.toString());
    }

    private static final int X = 1;
    private static final int O = 0;
    private static final int blank = -1;

    @org.junit.jupiter.api.Test
    @DisplayName("Horizontal win via real moves")
    public void testHorizontalWin() {
        Position p = Position.parsePosition(". . .\n. . .\n. . .", blank)
                .move(X, 0, 0)   // X
                .move(O, 2, 2)   // O
                .move(X, 0, 1)   // X
                .move(O, 2, 1)   // O
                .move(X, 0, 2);  // X completes top row
        assertTrue(p.winner().isPresent(), "should have a winner");
        assertEquals(X, p.winner().get(), "X should win");
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Vertical win via real moves")
    public void testVerticalWin() {
        Position p = Position.parsePosition(". . .\n. . .\n. . .", blank)
                .move(O, 0, 0)
                .move(X, 1, 1)
                .move(O, 1, 0)
                .move(X, 2, 2)
                .move(O, 2, 0);
        assertTrue(p.winner().isPresent(), "should have a winner");
        assertEquals(O, p.winner().get(), "O should win");
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Diagonal win via real moves")
    public void testDiagonalWin() {
        Position p = Position.parsePosition(". . .\n. . .\n. . .", blank)
                .move(X, 0, 0)
                .move(O, 0, 1)
                .move(X, 1, 1)
                .move(O, 0, 2)
                .move(X, 2, 2);
        assertTrue(p.winner().isPresent(), "should have a winner");
        assertEquals(X, p.winner().get(), "X should win");
    }

    @org.junit.jupiter.api.Test
    @DisplayName("No win (draw) when board full without three in a row")
    public void testNoWin() {
        // Fill board in a draw pattern
        Position p = Position.parsePosition(". . .\n. . .\n. . .", blank)
                .move(X, 0, 0).move(O, 0, 1)
                .move(X, 0, 2).move(O, 1, 1)
                .move(X, 1, 0).move(O, 1, 2)
                .move(X, 2, 1).move(O, 2, 0)
                .move(X, 2, 2);
        assertTrue(p.full(), "board should be full");
        assertFalse(p.winner().isPresent(), "no winner in a draw");
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Render and move-update correctness")
    public void testRenderAndMove() {
        Position start = Position.parsePosition(". . .\n. . .\n. . .", blank);
        Position after = start.move(O, 1, 1); // O plays center
        String[] lines = after.render().split("\n");
        assertEquals('.', lines[0].charAt(0));
        assertEquals('O', lines[1].charAt(2), "center should be O");
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Available moves count and consecutive-move prohibition")
    public void testMovesListAndConsecutive() {
        Position start = Position.parsePosition(". . .\n. . .\n. . .", blank);
        List<int[]> moves = start.moves(X);
        assertEquals(9, moves.size(), "initially 9 empty cells");

        // after X moves, X cannot move again
        Position p1 = start.move(X, 2, 2);
        assertThrows(RuntimeException.class, () -> p1.moves(X),
                "consecutive moves by same player");
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Illegal move into occupied cell")
    public void testIllegalMoveIntoOccupied() {
        Position start = Position.parsePosition(". . .\n. . .\n. . .", blank);
        Position p1 = start.move(X, 0, 0);
        assertThrows(RuntimeException.class, () -> p1.move(O, 0, 0),
                "cannot play into occupied cell");
    }

    @org.junit.jupiter.api.Test
    @DisplayName("Reflect across horizontal and vertical axes")
    public void testReflect() {
        Position p = Position.parsePosition(
                "X . .\n" +
                        ". O .\n" +
                        ". . X", blank);

        // Horizontal reflect (swap row 0 and row 2)
        Position refl0 = p.reflect(0);
        assertEquals(
                ". . X\n" +
                        ". O .\n" +
                        "X . .",
                refl0.render(),
                "horizontal reflect should flip top and bottom rows"
        );

        // Vertical reflect (swap col 0 and col 2)
        Position refl1 = p.reflect(1);
        assertEquals(
                ". . X\n" +
                        ". O .\n" +
                        "X . .",
                refl1.render(),
                "vertical reflect should flip left and right columns"
        );
    }


    @Test
    @DisplayName("Rotate 4×90° returns to original")
    public void testRotate() {
        Position p = Position.parsePosition("X . .\n. O .\n. . X", blank);
        Position r = p.rotate().rotate().rotate().rotate();
        assertEquals(p, r, "four rotations should yield original position");
    }

    @Test
    @DisplayName("Equals and hashCode contract")
    public void testEqualsAndHashCode() {
        Position a = Position.parsePosition("X . O\n. X .\nO . X", blank);
        Position b = Position.parsePosition("X . O\n. X .\nO . X", blank);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        Position c = a.move(O, 0, 1);
        assertNotEquals(a, c);
    }
}