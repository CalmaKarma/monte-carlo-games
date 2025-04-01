from abc import ABC, abstractmethod

class Game(ABC):
    @abstractmethod
    def get_legal_actions(self):
        pass

    @abstractmethod
    def take_action(self, action):
        pass

    @abstractmethod
    def is_terminal(self):
        pass

    @abstractmethod
    def get_result(self):
        pass

    @abstractmethod
    def clone(self):
        pass