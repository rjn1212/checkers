package com.gitgames.checkers.ai;

import com.gitgames.checkers.model.Board;
import com.gitgames.checkers.model.Move;
import com.gitgames.checkers.model.Player;

public interface AiStrategy {
    Move selectMove(Board board, Player player);
}
