import math
import random

class MCTSNode:
    def __init__(self, state, parent=None, action=None):
        self.state = state
        self.parent = parent
        self.action = action
        self.children = []
        self.visits = 0
        self.wins = 0

    def is_fully_expanded(self):
        return len(self.children) == len(self.state.get_legal_actions())

    def best_child(self, exploration_param=math.sqrt(2)):
        best_score = -float('inf')
        best_child = None
        for child in self.children:
            exploit = child.wins / child.visits if child.visits > 0 else 0
            explore = math.sqrt(math.log(self.visits) / child.visits) if child.visits > 0 else float('inf')
            score = exploit + exploration_param * explore
            if score > best_score:
                best_score = score
                best_child = child
        return best_child

class MCTS:
    def __init__(self, simulations=1000):
        self.simulations = simulations

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
                action = random.choice(state.get_legal_actions())
                state = state.take_action(action)

            # Backpropagation
            result = state.get_result()
            while node is not None:
                node.visits += 1
                node.wins += result
                node = node.parent

        return max(root.children, key=lambda c: c.visits).action
