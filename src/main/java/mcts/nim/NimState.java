package mcts.nim;

import mcts.core.Move;
import mcts.core.State;

import java.util.*;

/**
 * A Nim position: an array of pile‐sizes, plus who moved last.
 */
public class NimState implements State<NimGame> {
    private final NimGame game;
    private final int[] piles;
    private final int lastPlayer;

    /** Construct the root state (lastPlayer=1 so opener=0 moves first). */
    public NimState(NimGame game, int[] piles, int lastPlayer) {
        this.game = game;
        this.piles = piles;
        this.lastPlayer = lastPlayer;
    }

    @Override
    public NimGame game() {
        return game;
    }

    @Override
    public int player() {
        return 1 - lastPlayer;   // alternate
    }

    @Override
    public boolean isTerminal() {
        for (int p : piles) if (p > 0) return false;
        return true;
    }

    @Override
    public Optional<Integer> winner() {
        return isTerminal()
                ? Optional.of(lastPlayer)  // whoever took the last token
                : Optional.empty();
    }

    @Override
    public Random random() {
        return game.random;  // reuse game’s Random for shuffling
    }

    @Override
    public Collection<Move<NimGame>> moves(int player) {
        if (player == lastPlayer)
            throw new RuntimeException("consecutive moves by same player: " + player);
        List<Move<NimGame>> result = new ArrayList<>();
        for (int i = 0; i < piles.length; i++) {
            for (int r = 1; r <= piles[i]; r++) {
                result.add(new NimMove(player, i, r));
            }
        }
        return result;
    }

    @Override
    public State<NimGame> next(Move<NimGame> mv) {
        NimMove m = (NimMove) mv;
        int i = m.getPileIndex(), r = m.getRemoveCount();
        if (r < 1 || r > piles[i])
            throw new RuntimeException("invalid removal: " + r + " from pile " + i);
        int[] nextPiles = Arrays.copyOf(piles, piles.length);
        nextPiles[i] -= r;
        return new NimState(game, nextPiles, m.player());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NimState)) return false;
        return Arrays.equals(piles, ((NimState)o).piles)
                && lastPlayer == ((NimState)o).lastPlayer;
    }

    @Override
    public int hashCode() {
        return 31 * Arrays.hashCode(piles) + lastPlayer;
    }

    @Override
    public String toString() {
        return "piles=" + Arrays.toString(piles) + ", last=" + lastPlayer;
    }
}
