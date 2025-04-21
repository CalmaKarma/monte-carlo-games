// File: NimMCTS.java
package nim;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

import java.util.*;

/**
 * Monte Carlo Tree Search driver specialized for Nim.
 */
public class NimMCTS {

    private final Node<NimGame> root;
    private final double Cp;

    public NimMCTS(Node<NimGame> root, double Cp) {
        this.root = root;
        this.Cp   = Cp;
    }

    public void runSearch(int iterations) {
        for (int i = 0; i < iterations; i++) {
            List<Node<NimGame>> path = new ArrayList<>();
            Node<NimGame> node = root;
            path.add(node);

            // 1. Selection
            while (!node.isLeaf() && !node.children().isEmpty()) {
                node = selectUCT(node);
                path.add(node);
            }

            // 2. Expansion
            if (!node.isLeaf() && node.children().isEmpty()) {
                node.explore();
                node = node.children().iterator().next();
                path.add(node);
            }

            // 3. Simulation
            State<NimGame> st = node.state();
            int result = simulate(st);

            // 4. Back‑propagation
            for (Node<NimGame> n : path) {
                NimNode nn = (NimNode) n;
                nn.recordPlayout();
                nn.recordWin(result);
            }
        }
    }

    private Node<NimGame> selectUCT(Node<NimGame> node) {
        Node<NimGame> best = null;
        double bestVal = Double.NEGATIVE_INFINITY;
        double parentPlays = node.playouts();

        for (Node<NimGame> c : node.children()) {
            double plays = c.playouts();
            double uct = (plays == 0)
                    ? Double.POSITIVE_INFINITY
                    : ((double) c.wins() / plays)
                    + Cp * Math.sqrt(Math.log(parentPlays) / plays);

            if (uct > bestVal) {
                bestVal = uct;
                best = c;
            }
        }
        return best;
    }

    private int simulate(State<NimGame> s) {
        State<NimGame> cur = s;
        while (!cur.isTerminal()) {
            int p = cur.player();
            Move<NimGame> m = cur.chooseMove(p);
            cur = cur.next(m);
        }
        return cur.winner().orElse(-1);
    }

    /** Return the move (from the root) with the highest visit‐count. */
    public Move<NimGame> bestMove() {
        State<NimGame> rs = root.state();
        int player = rs.player();

        Node<NimGame> bestChild = null;
        int maxPlays = -1;
        for (Node<NimGame> c : root.children()) {
            if (c.playouts() > maxPlays) {
                maxPlays = c.playouts();
                bestChild = c;
            }
        }

        // find which Move leads into bestChild.state()
        for (Move<NimGame> m : rs.moves(player)) {
            if (rs.next(m).equals(bestChild.state())) {
                return m;
            }
        }
        throw new RuntimeException("bestMove: no matching move");
    }

    public static void main(String[] args) {
        NimGame game = new NimGame(1, 2, 3, 4, 5);
        NimNode root = new NimNode(game.start());
        NimMCTS mcts = new NimMCTS(root, Math.sqrt(2));
        mcts.runSearch(100_000);

        NimMove best = (NimMove) mcts.bestMove();
        System.out.printf("Best move: remove %d from pile %d by player %d%n",
                best.getRemoveCount(), best.getPileIndex(), best.player());
    }
}
