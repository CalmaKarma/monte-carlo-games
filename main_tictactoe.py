import argparse
from game_tictactoe import TicTacToe
from mcts_tictactoe import MCTS_TicTacToe
import os


def human_player(game_state):
    game_state.print_board()
    move = int(input("Your move (0-8): "))
    return move


def mcts_player(game_state, simulations=100000):
    mcts = MCTS_TicTacToe(simulations=simulations)
    return mcts.search(game_state)


def play_game(player1, player2):
    game = TicTacToe()
    current_player = player1 if game.player == 1 else player2

    while not game.is_terminal():
        move = current_player(game)
        game = game.take_action(move)
        current_player = player1 if game.player == 1 else player2

    game.print_board()
    result = game.get_result()
    if result == 1:
        print("X wins!")
    elif result == 0:
        print("O wins!")
    else:
        print("It's a draw!")


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description="Play TicTacToe with MCTS and/or humans.")
    parser.add_argument('--p1', choices=['human', 'mcts'], default='human', help='Player 1 (X)')
    parser.add_argument('--p2', choices=['human', 'mcts'], default='mcts', help='Player 2 (O)')
    args = parser.parse_args()

    player1 = human_player if args.p1 == 'human' else mcts_player
    player2 = human_player if args.p2 == 'human' else mcts_player

    play_game(player1, player2)
