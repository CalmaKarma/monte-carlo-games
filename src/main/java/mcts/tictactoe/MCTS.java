package mcts.tictactoe;

import mcts.core.Move;
import mcts.core.Node;
import mcts.core.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class to represent a Monte Carlo Tree Search for TicTacToe.
 */
public class MCTS {

    public static void main(String[] args) {
        TicTacToe game = new TicTacToe();
        TicTacToeNode rootNode = new TicTacToeNode(game.new TicTacToeState());
        MCTS mcts = new MCTS(rootNode, Math.sqrt(2));
        mcts.runSearch(100_000);
        TicTacToe.TicTacToeMove move = mcts.bestMove();
        System.out.printf("Best move: %d,%d by player %d\n", move.move()[0], move.move()[1], move.player());
    }

    /**
     * Run the MCTS algorithm for the given number of iterations.
     */
    public void runSearch(int iterations) {
        for (int i = 0; i < iterations; i++) {
            // 1. SELECTION
            List<Node<TicTacToe>> path = new ArrayList<>();
            Node<TicTacToe> node = root;
            path.add(node);
            while (!node.isLeaf() && !node.children().isEmpty()) {
                node = selectUCT(node);
                path.add(node);
            }

            // 2. EXPANSION
            if (!node.isLeaf() && node.children().isEmpty()) {
                node.explore();           // adds all immediate children
                // pick one child to simulate
                node = node.children().iterator().next();
                path.add(node);
            }

            // 3. SIMULATION (rollout)
            State<TicTacToe> state = node.state();
            int result = simulate(state);

            // 4. BACKPROPAGATION
            for (Node<TicTacToe> n : path) {
                TicTacToeNode tn = (TicTacToeNode)n;
                tn.recordPlayout();
                tn.recordWin(result);
            }
        }
    }

    /**
     * Select the child of 'node' with highest UCT value.
     */
    private Node<TicTacToe> selectUCT(Node<TicTacToe> node) {
        Node<TicTacToe> best = null;
        double bestValue = Double.NEGATIVE_INFINITY;
        double parentPlayouts = node.playouts();
        for (Node<TicTacToe> child : node.children()) {
            double childPlayouts = child.playouts();
            double uctValue;
            if (childPlayouts == 0) {
                uctValue = Double.POSITIVE_INFINITY;
            } else {
                double winRate = (double) child.wins() / childPlayouts;
                uctValue = winRate + Cp * Math.sqrt(Math.log(parentPlayouts) / childPlayouts);
            }
            if (uctValue > bestValue) {
                bestValue = uctValue;
                best = child;
            }
        }
        return best;
    }

    /**
     * Do a random playout from the given state to a terminal state.
     * @return the winner (0 or 1), or -1 for a draw.
     */
    private int simulate(State<TicTacToe> s) {
        State<TicTacToe> cur = s;
        while (!cur.isTerminal()) {
            int player = cur.player();
            Move<TicTacToe> m = cur.chooseMove(player);
            cur = cur.next(m);
        }
        Optional<Integer> winner = cur.winner();
        return winner.orElse(-1);
    }

    /**
     * After search, pick the most visited child as the best move.
     */
    public TicTacToe.TicTacToeMove bestMove() {
        State<TicTacToe> rootState = root.state();
        int player = rootState.player();

        // 1. pick the most‑visited child
        Node<TicTacToe> bestChild = null;
        int maxPlayouts = -1;
        for (Node<TicTacToe> c : root.children()) {
            if (c.playouts() > maxPlayouts) {
                maxPlayouts = c.playouts();
                bestChild = c;
            }
        }

        // 2. find which Move actually transitions from rootState → bestChild.state()
        for (Move<TicTacToe> m : rootState.moves(player)) {
            State<TicTacToe> s2 = rootState.next(m);
            if (s2.equals(bestChild.state())) {
                // this is the move you want
                return (TicTacToe.TicTacToeMove) m;
            }
        }

        throw new RuntimeException("bestMove: no matching move found");
    }


    public MCTS(Node<TicTacToe> root, double Cp) {
        this.root = root;
        this.Cp = Cp;
    }

    private final Node<TicTacToe> root;
    private final double Cp;
}