package com.gitgames.checkers.controller;

import com.gitgames.checkers.service.GameNotFoundException;
import com.gitgames.checkers.service.InvalidMoveException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GameNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleGameNotFound(GameNotFoundException e) {
        return Map.of("error", "GAME_NOT_FOUND", "message", e.getMessage());
    }

    @ExceptionHandler(InvalidMoveException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidMove(InvalidMoveException e) {
        return Map.of("error", "INVALID_MOVE", "message", e.getMessage());
    }
}
