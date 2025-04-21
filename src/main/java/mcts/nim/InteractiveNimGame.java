package mcts.nim;

import mcts.core.Move;
import mcts.core.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Interactive Nim driver: Human vs AI or AI vs Human or Human vs Human.
 */
public class InteractiveNimGame {

    private enum PlayerType { HUMAN, AI }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Choose control for player 0 and player 1
        System.out.print("Select player 0 (H = Human, A = AI): ");
        PlayerType p0 = promptType(scanner);
        System.out.print("Select player 1 (H = Human, A = AI): ");
        PlayerType p1 = promptType(scanner);

        // Configure initial piles
        System.out.print("Enter initial pile sizes (space-separated): ");
        String[] parts = scanner.nextLine().trim().split("\\s+");
        int[] piles = new int[parts.length];
        for (int i = 0; i < parts.length; i++) piles[i] = Integer.parseInt(parts[i]);

        NimGame game = new NimGame(piles);
        State<NimGame> state = game.start();
        int currentPlayer = game.opener();

        // Main game loop
        while (!state.isTerminal()) {
            System.out.println("\nCurrent state: " + state);

            Move<NimGame> move;
            PlayerType type = (currentPlayer == 0 ? p0 : p1);
            if (type == PlayerType.HUMAN) {
                move = humanMove(scanner, state, currentPlayer);
            } else {
                System.out.println("AI is thinking...");
                // Run MCTS
                NimNode root = new NimNode(state);
                NimMCTS mcts = new NimMCTS(root, Math.sqrt(2));
                mcts.runSearch(5_000);
                move = mcts.bestMove();
                NimMove nm = (NimMove) move;
                System.out.printf("AI plays: remove %d from pile %d\n",
                        nm.getRemoveCount(), nm.getPileIndex());
            }

            state = state.next(move);
            currentPlayer = state.player();
        }

        // Game over
        System.out.println("\nFinal state: " + state);
        Optional<Integer> winner = state.winner();
        if (winner.isPresent()) {
            System.out.println("Winner: player " + winner.get());
        } else {
            System.out.println("Draw (impossible in Nim)");
        }

        scanner.close();
    }

    private static PlayerType promptType(Scanner scanner) {
        while (true) {
            String line = scanner.nextLine().trim().toUpperCase();
            if (line.startsWith("H")) return PlayerType.HUMAN;
            if (line.startsWith("A")) return PlayerType.AI;
            System.out.print("Please enter H or A: ");
        }
    }

    private static Move<NimGame> humanMove(Scanner scanner, State<NimGame> state, int player) {
        List<Move<NimGame>> legal = new ArrayList<>(state.moves(player));
        while (true) {
            System.out.print("Enter your move as 'pileIndex removeCount': ");
            String[] tokens = scanner.nextLine().trim().split("\\s+");
            try {
                int pi = Integer.parseInt(tokens[0]);
                int rc = Integer.parseInt(tokens[1]);
                for (Move<NimGame> m : legal) {
                    NimMove nm = (NimMove) m;
                    if (nm.getPileIndex() == pi && nm.getRemoveCount() == rc) {
                        return nm;
                    }
                }
                System.out.println("Invalid move. Legal moves: ");
                for (Move<NimGame> m : legal) System.out.println("  " + m);
            } catch (Exception e) {
                System.out.println("Invalid input format. Example: '1 3' to remove 3 tokens from pile 1.");
            }
        }
    }
}
