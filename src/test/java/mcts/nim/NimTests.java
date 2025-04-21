package mcts.nim;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import mcts.core.State;
import mcts.core.Move;
import mcts.core.Node;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for NimGame, NimState, NimMove, and NimNode.
 */
public class NimTests {

    private NimGame game;

    @BeforeEach
    public void setup() {
        // standard 3-4-5 Nim for most tests
        game = new NimGame(3, 4, 5);
    }

    @Test
    public void testOpenerAndStart() {
        State<NimGame> s = game.start();
        // opener is player 0
        assertEquals(0, game.opener());
        // start state player() should be opener (0)
        assertEquals(game.opener(), s.player());
        // start state should not be terminal
        assertFalse(s.isTerminal());
        assertTrue(s.winner().isEmpty());
    }

    @Test
    public void testMovesAndNext() {
        State<NimGame> s = game.start();
        Collection<Move<NimGame>> moves = s.moves(s.player());
        // piles {3,4,5} -> total moves = 3+4+5 = 12
        assertEquals(12, moves.size());

        // pick a move and apply next
        Move<NimGame> m = moves.iterator().next();
        State<NimGame> s2 = s.next(m);

        // s2 should NOT be terminal
        assertFalse(s2.isTerminal());
        // no winner yet
        assertTrue(s2.winner().isEmpty());
        // turn should pass to the other player
        assertEquals(1 - s.player(), s2.player());
    }

    @Test
    public void testTerminalAndWinner() {
        // create a state with single pile of size 1
        NimGame game1 = new NimGame(1);
        State<NimGame> s = game1.start();
        // initial state not terminal
        assertFalse(s.isTerminal());
        // one legal move: remove 1 from pile 0
        Move<NimGame> only = s.moves(s.player()).iterator().next();
        State<NimGame> t = s.next(only);
        // now terminal
        assertTrue(t.isTerminal());
        // winner should be the mover
        assertTrue(t.winner().isPresent());
        assertEquals(only.player(), t.winner().get());
    }

    @Test
    public void testInvalidConsecutiveMove() {
        State<NimGame> s = game.start();
        // using same player twice should throw
        int p = s.player();
        // get a valid move
        Move<NimGame> m = s.moves(p).iterator().next();
        State<NimGame> s2 = s.next(m);
        // now s2.player() != p
        assertNotEquals(p, s2.player());
        // calling moves with wrong player
        assertThrows(RuntimeException.class, () -> s2.moves(p));
    }

    @Test
    public void testMoveEqualityAndHashCode() {
        NimMove m1 = new NimMove(0, 1, 3);
        NimMove m2 = new NimMove(0, 1, 3);
        NimMove m3 = new NimMove(1, 1, 3);
        assertEquals(m1, m2);
        assertEquals(m1.hashCode(), m2.hashCode());
        assertNotEquals(m1, m3);
    }

    @Test
    public void testNodeExpansionAndBackpropagation() {
        // game1: single pile of 1
        NimGame game1 = new NimGame(1);
        State<NimGame> rootState = game1.start();
        NimNode root = new NimNode(rootState);
        // root not terminal
        assertFalse(root.isLeaf());
        assertTrue(root.children().isEmpty());
        // expand
        root.explore();
        // now one child
        assertEquals(1, root.children().size());
        Node<NimGame> child = root.children().iterator().next();
        // child should be leaf and terminal
        assertTrue(child.isLeaf());
        assertEquals(1, child.playouts());
        assertEquals(2, child.wins(), "winning leaf should have 2 wins");
        // back-propagate to root
        root.backPropagate();
        assertEquals(1, root.playouts());
        assertEquals(2, root.wins());
    }

    @Test
    public void testMCTSSimulationPlayouts() {
        // verify MCTS runs without error and picks a move
        NimNode root = new NimNode(game.start());
        NimMCTS mcts = new NimMCTS(root, Math.sqrt(2));
        mcts.runSearch(100);
        Move<NimGame> m = mcts.bestMove();
        assertNotNull(m);
    }
}
