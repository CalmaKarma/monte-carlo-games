package mcts.nim;

import mcts.core.Move;
import mcts.core.State;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Benchmarking harness for Nim MCTS.
 */
public class NimBenchmark {

    private static final int[] BUDGETS = {10, 30, 100, 300, 1_000, 3_000, 10_000, 30_000, 100_000};
    private static final double[] CPS = {0.5, 1, Math.sqrt(2), 2.0};
    private static final int GAMES_PER_SETTING = 1000;
    private static final int STABILITY_RUNS = 50;

    // Example initial piles for the benchmark
    private static final int[] INITIAL_PILES = {3, 4, 5};

    public static void main(String[] args) {
        System.out.println("=== Nim MCTS Benchmark ===");
        benchmarkWinRates();
        benchmarkStability();
        benchmarkPlayoutTiming(10_000);
    }

    /**
     * Run MCTS vs. random over varying budgets and Cp values.
     */
    private static void benchmarkWinRates() {
        System.out.println("\n-- Win/Draw/Loss vs Random (Nim) --");
        System.out.println("Budget\tCp\tWins\tDraws\tLosses\tAvgMoveTime(ms)");
        for (int budget : BUDGETS) {
            for (double cp : CPS) {
                int wins = 0, draws = 0, losses = 0;
                long totalMoveTime = 0;
                long totalMoves = 0;
                for (int g = 0; g < GAMES_PER_SETTING; g++) {
                    NimGame game = new NimGame(INITIAL_PILES);
                    State<NimGame> state = game.start();
                    int player = game.opener();

                    while (!state.isTerminal()) {
                        if (player == game.opener()) {
                            // MCTS move
                            NimNode root = new NimNode(state);
                            NimMCTS mcts = new NimMCTS(root, cp);
                            long t0 = System.nanoTime();
                            mcts.runSearch(budget);
                            Move<NimGame> move = mcts.bestMove();
                            long t1 = System.nanoTime();
                            totalMoveTime += (t1 - t0);
                            totalMoves++;
                            state = state.next(move);
                        } else {
                            // random move
                            Move<NimGame> move = state.chooseMove(player);
                            state = state.next(move);
                        }
                        player = state.player();
                    }

                    Optional<Integer> winner = state.winner();
                    if (winner.isEmpty()) {
                        draws++;
                    } else if (winner.get() == game.opener()) {
                        wins++;
                    } else {
                        losses++;
                    }
                }
                double avgMoveMs = (totalMoveTime / 1e6) / (double) totalMoves;
                System.out.printf("%d\t%.2f\t%d\t%d\t%d\t%.3f%n",
                        budget, cp, wins, draws, losses, avgMoveMs);
            }
        }
    }

    /**
     * Stability: measure how often the first move repeats.
     */
    private static void benchmarkStability() {
        System.out.println("\n-- Opening‐Move Stability (Nim) --");
        System.out.println("Budget\tCp\tMostCommonMove\tFreq%");
        NimGame game = new NimGame(INITIAL_PILES);
        State<NimGame> rootState = game.start();

        for (int budget : BUDGETS) {
            for (double cp : CPS) {
                Map<String, Integer> counts = new HashMap<>();
                for (int run = 0; run < STABILITY_RUNS; run++) {
                    NimNode root = new NimNode(rootState);
                    NimMCTS mcts = new NimMCTS(root, cp);
                    mcts.runSearch(budget);
                    Move<NimGame> m = mcts.bestMove();
                    String key = m.toString();
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

    /**
     * Microbenchmark: average time per simulation for a single runSearch call.
     */
    private static void benchmarkPlayoutTiming(int budget) {
        System.out.println("\n-- Playout Timing (Nim) --");
        System.out.println("Budget\tAvgTimePerPlayout(µs)");
        // warm-up
        NimNode warm = new NimNode(new NimGame(INITIAL_PILES).start());
        new NimMCTS(warm, Math.sqrt(2)).runSearch(budget);

        final int REPS = 100;
        long totalTime = 0;
        for (int i = 0; i < REPS; i++) {
            NimNode root = new NimNode(new NimGame(INITIAL_PILES).start());
            NimMCTS mcts = new NimMCTS(root, Math.sqrt(2));
            long t0 = System.nanoTime();
            mcts.runSearch(budget);
            long t1 = System.nanoTime();
            totalTime += (t1 - t0);
        }
        double avgUs = (totalTime / 1e3) / (double) (budget * REPS);
        System.out.printf("%d\t%.3f%n", budget, avgUs);
    }
}
