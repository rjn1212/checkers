package com.gitgames.checkers.controller.dto;

import com.gitgames.checkers.model.AiDifficulty;
import com.gitgames.checkers.model.Player;

public record CreateGameRequest(
    AiDifficulty aiDifficulty,
    Player playerSide
) {}
