# Monte Carlo Tree Search for Tic‑Tac‑Toe and Nim

## Tic-Tac-Toe

For the interactive Tic-Tac-Toe game, run `src/java/mcts/tictactoe/InteractiveTicTacToe.java`. 

1. Run the `InteractiveTicTacToe` class to start the game.
2. Set Human(H) or AI(A) for each player.
   - If you set both players as AI, the game will be played automatically.
   - If you set both players as Human, you can play against each other.
   - If you set one player as AI and the other as Human, you can play against the AI.
3. When it is your turn, enter the indices of the row and column, e.g. `0 1` to place a mark in the first row and second column.
    ```
        0     1     2
    0 (0 0) (0 1) (0 2)
    1 (1 0) (1 1) (1 2)
    2 (2 0) (2 1) (2 2)
    ```
4. A player wins when they have three marks in a row, column, or diagonal.


For the benchmark, run `src/java/mcts/tictactoe/TicTacToeBenchmark.java`.

You can adjust the following parameters in the `TicTacToeBenchmark` class:
```java
private static final int[] BUDGETS = {10, 30, 100, 300, 1_000, 3_000, 10_000, 30_000, 100_000};
private static final double[] CPS = {0.5, 1, Math.sqrt(2), 2.0};
private static final int GAMES_PER_SETTING = 1000;
private static final int STABILITY_RUNS = 50;
```

The benchmark consists of three parts:
1. Playout Time: It measures the average time taken for a playout.
2. Win Rate: It measures the win rate of the AI against a random player.
    - For each pair of `BUDGET` and `CP`, the benchmark runs `GAMES_PER_SETTING` games and counts the outcome.
3. Stability: It measures the stability of the AI's performance.
    - For each pair of `BUDGET` and `CP`, the benchmark runs `STABILITY_RUNS` games and calculates the frequency of the most frequent outcome.

## Nim

For the interactive Nim game, run `src/java/mcts/nim/InteractiveNimGame.java`. 

1. Run the `InteractiveNimGame` class to start the game.
2. Set Human(H) or AI(A) for each player.
   - If you set both players as AI, the game will be played automatically.
   - If you set both players as Human, you can play against each other.
   - If you set one player as AI and the other as Human, you can play against the AI.
3. Enter the pile sizes, e.g. `3 4 5`.
4. When it is your turn, enter the pile index and the number of stones to remove, e.g. `0 2` to remove 2 stones from pile 0. Note that the index starts from 0.
5. A player wins when they remove the last item.

For the benchmark, run `src/java/mcts/nim/NimBenchmark.java`.

You can adjust the following parameters in the `NimBenchmark` class:
```java
private static final int[] BUDGETS = {10, 30, 100, 300, 1_000, 3_000, 10_000, 30_000, 100_000};
private static final double[] CPS = {0.5, 1, Math.sqrt(2), 2.0};
private static final int GAMES_PER_SETTING = 1000;
private static final int STABILITY_RUNS = 50;

private static final int[] INITIAL_PILES = {3, 4, 5};
```

You need to set the initial piles in the `INITIAL_PILES` array. This will be used throughout the benchmark.

Like for Tic-Tac-Toe, the benchmark consists of three parts:
1. Playout Time: It measures the average time taken for a playout.
2. Win Rate: It measures the win rate of the AI against a random player.
    - For each pair of `BUDGET` and `CP`, the benchmark runs `GAMES_PER_SETTING` games and counts the outcome.
3. Stability: It measures the stability of the AI's performance.
    - For each pair of `BUDGET` and `CP`, the benchmark runs `STABILITY_RUNS` games and calculates the frequency of the most frequent outcome.
