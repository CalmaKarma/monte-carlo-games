package mcts.tictactoe;

import mcts.core.Move;
import mcts.core.State;

import java.util.*;

/**
 * Benchmarking harness for TicTacToe MCTS.
 */
public class TicTacToeBenchmark {

    private static final int[] BUDGETS = {10, 30,100,300,1_000, 3_000, 10_000, 30_000,100_000};
    private static final double[] CPS = {0.5, 1, Math.sqrt(2), 2.0};
    private static final int GAMES_PER_SETTING = 1000;
    private static final int STABILITY_RUNS = 50;

    public static void main(String[] args) {
        System.out.println("=== TicTacToe MCTS Benchmark ===");
        benchmarkWinRates();
        benchmarkStability();
        benchmarkPlayoutTiming(10_000);
    }

    /** Run MCTS vs. random over varying budgets and Cp values. */
    private static void benchmarkWinRates() {
        System.out.println("\n-- Win/Draw/Loss vs Random --");
        System.out.println("Budget\tCp\tWins\tDraws\tLosses\tAvgMoveTime(ms)");
        for (int budget : BUDGETS) {
            for (double cp : CPS) {
                int wins = 0, draws = 0, losses = 0;
                long totalMoveTime = 0, totalMoves = 0;
                for (int g = 0; g < GAMES_PER_SETTING; g++) {
                    // play one game: MCTS is X (1), random is O (0)
                    TicTacToe game = new TicTacToe();
                    State<TicTacToe> state = game.start();
                    int player = game.opener();  // X starts
                    TicTacToe.TicTacToeMove lastMctsMove = null;

                    while (!state.isTerminal()) {
                        if (player == TicTacToe.X) {
                            // MCTS move
                            TicTacToeNode root = new TicTacToeNode(state);
                            MCTS mcts = new MCTS(root, cp);
                            long t0 = System.nanoTime();
                            mcts.runSearch(budget);
                            lastMctsMove = mcts.bestMove();
                            long t1 = System.nanoTime();
                            totalMoveTime += (t1 - t0);
                            totalMoves++;
                            state = state.next(lastMctsMove);
                        } else {
                            // random move
                            Move<TicTacToe> m = state.chooseMove(player);
                            state = state.next(m);
                        }
                        player = 1 - player;
                    }

                    Optional<Integer> winner = state.winner();
                    if (winner.isEmpty()) {
                        draws++;
                    } else if (winner.get() == TicTacToe.X) {
                        wins++;
                    } else {
                        losses++;
                    }
                }
                double avgMoveMs = (totalMoveTime / 1e6) / (double) totalMoves;
                System.out.printf(
                        "%d\t%.2f\t%d\t%d\t%d\t%.3f%n",
                        budget, cp, wins, draws, losses, avgMoveMs
                );
            }
        }
    }

    /** Stability: measure how often the opening move repeats. */
    private static void benchmarkStability() {
        System.out.println("\n-- Opening‐Move Stability --");
        System.out.println("Budget\tCp\tMostCommonMove\tFreq%");
        State<TicTacToe> rootState = new TicTacToe().start();
        int opener = rootState.game().opener();

        for (int budget : BUDGETS) {
            for (double cp : CPS) {
                Map<String, Integer> counts = new HashMap<>();
                for (int run = 0; run < STABILITY_RUNS; run++) {
                    TicTacToeNode root = new TicTacToeNode(rootState);
                    MCTS mcts = new MCTS(root, cp);
                    mcts.runSearch(budget);
                    TicTacToe.TicTacToeMove m = mcts.bestMove();
                    String key = m.move()[0] + "," + m.move()[1];
                    counts.merge(key, 1, Integer::sum);
                }
                // find most common
                String bestMove = counts.entrySet().stream()
                        .max(Map.Entry.comparingByValue())
                        .map(Map.Entry::getKey).orElse("?");
                int freq = counts.getOrDefault(bestMove, 0);
                double pct = 100.0 * freq / STABILITY_RUNS;
                System.out.printf("%d\t%.2f\t%s\t%.1f%%%n", budget, cp, bestMove, pct);
            }
        }
    }

    /** Microbenchmark: average time per simulation for a single runSearch call. */
    private static void benchmarkPlayoutTiming(int budget) {
        System.out.println("\n-- Playout Timing --");
        System.out.println("Budget\tAvgTimePerPlayout(µs)");
        // warm‑up
        TicTacToeNode warm = new TicTacToeNode(new TicTacToe().start());
        new MCTS(warm, Math.sqrt(2)).runSearch(budget);

        final int REPS = 100;
        long totalTime = 0, totalPlayouts = 0;
        for (int i = 0; i < REPS; i++) {
            TicTacToeNode root = new TicTacToeNode(new TicTacToe().start());
            MCTS mcts = new MCTS(root, Math.sqrt(2));
            long t0 = System.nanoTime();
            mcts.runSearch(budget);
            long t1 = System.nanoTime();
            totalTime += (t1 - t0);
            totalPlayouts += budget;
        }
        double avgUs = (totalTime / 1e3) / (double) totalPlayouts;
        System.out.printf("%d\t%.3f%n", budget, avgUs);
    }
}
