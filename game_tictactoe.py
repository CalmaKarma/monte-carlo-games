from game_abc import Game

class TicTacToe(Game):
    def __init__(self, board=None, player=1):
        self.board = board if board else [0] * 9  # 0 = empty, 1 = X, -1 = O
        self.player = player  # 1 = X, -1 = O

    def get_legal_actions(self):
        return [i for i in range(9) if self.board[i] == 0]

    def take_action(self, action):
        new_board = self.board[:]
        new_board[action] = self.player
        return TicTacToe(new_board, -self.player)

    def is_terminal(self):
        return self.get_result() is not None or all(x != 0 for x in self.board)

    def get_result(self):
        wins = [
            (0, 1, 2), (3, 4, 5), (6, 7, 8),  # rows
            (0, 3, 6), (1, 4, 7), (2, 5, 8),  # columns
            (0, 4, 8), (2, 4, 6)              # diagonals
        ]
        for i, j, k in wins:
            if self.board[i] == self.board[j] == self.board[k] != 0:
                return 1 if self.board[i] == 1 else 0
        if all(x != 0 for x in self.board):
            return 0.5  # draw
        return None

    def clone(self):
        return TicTacToe(self.board[:], self.player)

    def print_board(self):
        symbols = {1: 'X', -1: 'O', 0: ' '}
        for i in range(0, 9, 3):
            print('|'.join(symbols[self.board[j]] for j in range(i, i+3)))
            if i < 6:
                print('-----')
