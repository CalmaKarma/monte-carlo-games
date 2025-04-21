package mcts.nim;

import mcts.core.Move;

/**
 * A single Nim move: player removes removeCount tokens from pileIndex.
 */
public class NimMove implements Move<NimGame> {
    private final int player, pileIndex, removeCount;

    public NimMove(int player, int pileIndex, int removeCount) {
        this.player = player;
        this.pileIndex = pileIndex;
        this.removeCount = removeCount;
    }

    @Override
    public int player() {
        return player;
    }

    public int getPileIndex() {
        return pileIndex;
    }

    public int getRemoveCount() {
        return removeCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NimMove)) return false;
        NimMove m = (NimMove) o;
        return player == m.player
                && pileIndex == m.pileIndex
                && removeCount == m.removeCount;
    }

    @Override
    public int hashCode() {
        int h = player;
        h = 31*h + pileIndex;
        h = 31*h + removeCount;
        return h;
    }

    @Override
    public String toString() {
        return String.format("P%d: remove %d from pile %d", player, removeCount, pileIndex);
    }
}
