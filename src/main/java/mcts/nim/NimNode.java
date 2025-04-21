package mcts.nim;

import mcts.core.Node;
import mcts.core.State;

import java.util.*;

/**
 * MCTS tree node for Nim.
 */
public class NimNode implements Node<NimGame> {
    private final State<NimGame> state;
    private final List<Node<NimGame>> children = new ArrayList<>();
    private int wins, playouts;

    public NimNode(State<NimGame> state) {
        this.state = state;
        initializeLeaf();
    }

    private void initializeLeaf() {
        if (state.isTerminal()) {
            playouts = 1;
            wins = state.winner().isPresent() ? 2 : 1;
        }
    }

    @Override public boolean isLeaf()               { return state.isTerminal(); }
    @Override public State<NimGame> state()         { return state; }
    @Override public boolean white()                { return state.player() == state.game().opener(); }
    @Override public Collection<Node<NimGame>> children() { return children; }

    @Override
    public void addChild(State<NimGame> s) {
        children.add(new NimNode(s));
    }

    @Override
    public void backPropagate() {
        wins = 0;
        playouts = 0;
        for (Node<NimGame> c : children) {
            wins     += c.wins();
            playouts += c.playouts();
        }
    }

    @Override public int wins()      { return wins; }
    @Override public int playouts()  { return playouts; }

    /** Call on every visit. */
    public void recordPlayout() {
        playouts++;
    }

    /**
     * @param winner 0 or 1 for who won, or -1 for draw.
     */
    public void recordWin(int winner) {
        if (winner < 0) {
            wins += 1;       // draw
        } else {
            int mover = 1 - state.player();  // who *just* moved
            if (winner == mover) wins += 2;
        }
    }
}
