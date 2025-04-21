package nim;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Game;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

import java.util.Arrays;
import java.util.Random;

/**
 * Models the overall Nim game.
 */
public class NimGame implements Game<NimGame> {

    private final int opener = 0;        // player 0 always starts
    private final int[] initialPiles;    // sizes of each pile
    final Random random;

    /** Create a Nim game with the given pile sizes and a fresh Random. */
    public NimGame(int... piles) {
        this.random = new Random();
        this.initialPiles = Arrays.copyOf(piles, piles.length);
    }

    /** Which player moves first. */
    @Override
    public int opener() {
        return opener;
    }

    /** The very first state: all piles as given, lastPlayer=1 so opener(0) moves next. */
    @Override
    public State<NimGame> start() {
        return new NimState(this, Arrays.copyOf(initialPiles, initialPiles.length), /*lastPlayer=*/1);
    }
}
