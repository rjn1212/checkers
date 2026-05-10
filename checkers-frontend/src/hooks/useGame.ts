import { useReducer, useCallback } from 'react';
import type { GameState, MoveData, Position, AiDifficulty, Player } from '../types/game';
import * as api from '../api/gameApi';

interface GameStore {
  game: GameState | null;
  selectedPiece: Position | null;
  legalMoves: MoveData[];
  mustJump: boolean;
  loading: boolean;
  error: string | null;
  aiThinking: boolean;
}

type Action =
  | { type: 'SET_LOADING' }
  | { type: 'SET_ERROR'; error: string }
  | { type: 'CLEAR_ERROR' }
  | { type: 'SET_GAME'; game: GameState }
  | { type: 'SELECT_PIECE'; position: Position; moves: MoveData[]; mustJump: boolean }
  | { type: 'DESELECT_PIECE' }
  | { type: 'AI_THINKING' }
  | { type: 'MOVE_COMPLETE'; game: GameState }
  | { type: 'RESET' };

const initialState: GameStore = {
  game: null,
  selectedPiece: null,
  legalMoves: [],
  mustJump: false,
  loading: false,
  error: null,
  aiThinking: false,
};

function reducer(state: GameStore, action: Action): GameStore {
  switch (action.type) {
    case 'SET_LOADING':
      return { ...state, loading: true, error: null };
    case 'SET_ERROR':
      return { ...state, loading: false, error: action.error, aiThinking: false };
    case 'CLEAR_ERROR':
      return { ...state, error: null };
    case 'SET_GAME':
      return { ...state, game: action.game, loading: false, selectedPiece: null, legalMoves: [], aiThinking: false };
    case 'SELECT_PIECE':
      return { ...state, selectedPiece: action.position, legalMoves: action.moves, mustJump: action.mustJump };
    case 'DESELECT_PIECE':
      return { ...state, selectedPiece: null, legalMoves: [] };
    case 'AI_THINKING':
      return { ...state, aiThinking: true, selectedPiece: null, legalMoves: [] };
    case 'MOVE_COMPLETE':
      return { ...state, game: action.game, selectedPiece: null, legalMoves: [], aiThinking: false };
    case 'RESET':
      return initialState;
    default:
      return state;
  }
}

export function useGame() {
  const [state, dispatch] = useReducer(reducer, initialState);

  const newGame = useCallback(async (difficulty: AiDifficulty, side: Player) => {
    dispatch({ type: 'SET_LOADING' });
    try {
      const game = await api.createGame({ aiDifficulty: difficulty, playerSide: side });
      dispatch({ type: 'SET_GAME', game });
    } catch (e) {
      dispatch({ type: 'SET_ERROR', error: (e as Error).message });
    }
  }, []);

  const selectPiece = useCallback(async (col: number, row: number) => {
    if (!state.game) return;
    try {
      const response = await api.getLegalMoves(state.game.gameId, col, row);
      if (response.moves.length > 0) {
        dispatch({
          type: 'SELECT_PIECE',
          position: { col, row },
          moves: response.moves,
          mustJump: response.mustJump,
        });
      }
    } catch (e) {
      dispatch({ type: 'SET_ERROR', error: (e as Error).message });
    }
  }, [state.game]);

  const deselectPiece = useCallback(() => {
    dispatch({ type: 'DESELECT_PIECE' });
  }, []);

  const makeMove = useCallback(async (to: Position) => {
    if (!state.game || !state.selectedPiece) return;
    dispatch({ type: 'AI_THINKING' });
    try {
      const response = await api.makeMove(state.game.gameId, {
        from: state.selectedPiece,
        to,
      });
      const updatedGame: GameState = {
        ...state.game,
        board: response.board,
        phase: response.phase,
        currentTurn: response.currentTurn,
        capturedCount: response.capturedCount,
        winner: response.winner,
        winReason: response.winReason,
      };
      dispatch({ type: 'MOVE_COMPLETE', game: updatedGame });
    } catch (e) {
      dispatch({ type: 'SET_ERROR', error: (e as Error).message });
    }
  }, [state.game, state.selectedPiece]);

  const resetGame = useCallback(() => {
    dispatch({ type: 'RESET' });
  }, []);

  return {
    ...state,
    newGame,
    selectPiece,
    deselectPiece,
    makeMove,
    resetGame,
  };
}
