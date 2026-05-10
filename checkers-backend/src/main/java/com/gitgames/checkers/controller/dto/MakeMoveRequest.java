package com.gitgames.checkers.controller.dto;

public record MakeMoveRequest(
    PositionDto from,
    PositionDto to
) {}
