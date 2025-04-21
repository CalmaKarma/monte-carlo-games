package tictactoe;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

import java.util.Optional;
import java.util.Scanner;

/**
 * Interactive TicTacToe driver allowing Human vs MCTS, MCTS vs Human, or Human vs Human.
 */
public class InteractiveTicTacToe {

    private enum PlayerType { HUMAN, MCTS }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // choose types for X (1) and O (0)
        System.out.print("Select X player (H = Human, A = AI): ");
        PlayerType xType = promptType(scanner);
        System.out.print("Select O player (H = Human, A = AI): ");
        PlayerType oType = promptType(scanner);

        TicTacToe game = new TicTacToe();
        State<TicTacToe> state = game.start();
        int currentPlayer = game.opener();  // X starts

        // loop until terminal
        while (!state.isTerminal()) {
            System.out.println("\nCurrent board:");
            System.out.println(((TicTacToe.TicTacToeState)state).position().render());

            Move<TicTacToe> move;
            if (playerType(currentPlayer, xType, oType) == PlayerType.HUMAN) {
                move = humanMove(scanner, state, currentPlayer);
            } else {
                System.out.println("AI is thinking...");
                // simple MCTS with fixed iterations
                TicTacToeNode root = new TicTacToeNode(state);
                MCTS mcts = new MCTS(root, Math.sqrt(2));
                mcts.runSearch(100_000);
                move = mcts.bestMove();
                System.out.printf("AI plays: %d,%d\n", ((TicTacToe.TicTacToeMove)move).move()[0], ((TicTacToe.TicTacToeMove)move).move()[1]);
            }

            state = state.next(move);
            currentPlayer = 1 - currentPlayer;
        }

        // final result
        System.out.println("\nFinal board:");
        System.out.println(((TicTacToe.TicTacToeState)state).position().render());
        Optional<Integer> winner = state.winner();
        if (winner.isPresent()) {
            System.out.println("Winner: " + (winner.get() == TicTacToe.X ? "X" : "O"));
        } else {
            System.out.println("Draw");
        }
        scanner.close();
    }

    private static PlayerType promptType(Scanner scanner) {
        while (true) {
            String line = scanner.nextLine().trim().toUpperCase();
            if (line.startsWith("H")) return PlayerType.HUMAN;
            if (line.startsWith("A")) return PlayerType.MCTS;
            System.out.print("Please enter H or A: ");
        }
    }

    private static PlayerType playerType(int player, PlayerType xType, PlayerType oType) {
        return player == TicTacToe.X ? xType : oType;
    }

    private static Move<TicTacToe> humanMove(Scanner scanner, State<TicTacToe> state, int player) {
        while (true) {
            System.out.print("Enter your move as 'row col' (0-based): ");
            String[] parts = scanner.nextLine().trim().split("\\s+");
            try {
                int r = Integer.parseInt(parts[0]);
                int c = Integer.parseInt(parts[1]);
                // validate
                for (Move<TicTacToe> m : state.moves(player)) {
                    TicTacToe.TicTacToeMove tm = (TicTacToe.TicTacToeMove)m;
                    int[] rc = tm.move();
                    if (rc[0] == r && rc[1] == c) return tm;
                }
                System.out.println("Invalid move. Try again.");
            } catch (Exception e) {
                System.out.println("Invalid input. Try again.");
            }
        }
    }
}
