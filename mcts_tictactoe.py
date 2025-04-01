from mcts import MCTS, MCTSNode
import random
import pickle

class MCTS_TicTacToe(MCTS):
    def __init__(self, simulations=1000):
        super().__init__(simulations)
        self.state_values = {}  # board hash -> (wins, visits)

    def state_hash(self, state):
        return tuple(state.board), state.player

    def search(self, initial_state):
        root = MCTSNode(initial_state)

        for _ in range(self.simulations):
            node = root
            state = initial_state.clone()

            # Selection
            while not state.is_terminal() and node.is_fully_expanded():
                node = node.best_child()
                state = state.take_action(node.action)

            # Expansion
            if not state.is_terminal():
                legal_actions = state.get_legal_actions()
                tried_actions = [child.action for child in node.children]
                untried_actions = [a for a in legal_actions if a not in tried_actions]

                action = random.choice(untried_actions)
                new_state = state.take_action(action)
                child_node = MCTSNode(new_state, parent=node, action=action)
                node.children.append(child_node)
                node = child_node
                state = new_state

            # Simulation
            while not state.is_terminal():
                action = self.heuristic_policy(state)
                state = state.take_action(action)

            # Backpropagation
            result = state.get_result()
            while node is not None:
                node.visits += 1
                node.wins += result
                h = self.state_hash(node.state)
                if h in self.state_values:
                    w, v = self.state_values[h]
                    self.state_values[h] = (w + result, v + 1)
                else:
                    self.state_values[h] = (result, 1)
                node = node.parent

        return max(root.children, key=lambda c: c.visits).action

    def heuristic_policy(self, state):
        actions = state.get_legal_actions()
        # Win if possible
        for action in actions:
            if state.take_action(action).get_result() == 1:
                return action
        # Block opponent's win
        for action in actions:
            if state.take_action(action).get_result() == 0:
                return action
        # Otherwise random
        return random.choice(actions)

    def save(self, filename):
        with open(filename, 'wb') as f:
            pickle.dump(self.state_values, f)

    def load(self, filename):
        with open(filename, 'rb') as f:
            self.state_values = pickle.load(f)
