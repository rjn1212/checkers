import { useGame } from '../../hooks/useGame';
import { Board } from '../Board/Board';
import { GameInfo } from './GameInfo';
import { NewGameDialog } from '../Common/NewGameDialog';
import { GameOverDialog } from '../Common/GameOverDialog';
import type { Position } from '../../types/game';
import './GameView.css';

export function GameView() {
  const {
    game,
    selectedPiece,
    legalMoves,
    loading,
    error,
    aiThinking,
    newGame,
    selectPiece,
    deselectPiece,
    makeMove,
    resetGame,
  } = useGame();

  const handleSquareClick = (col: number, row: number) => {
    if (!game || game.phase === 'GAME_OVER' || aiThinking) return;

    const isDestination = legalMoves.some((m) => m.to.col === col && m.to.row === row);

    if (isDestination) {
      makeMove({ col, row } as Position);
      return;
    }

    const piece = game.board.pieces.find((p) => p.col === col && p.row === row);
    if (piece && piece.owner === game.humanSide && piece.owner === game.currentTurn) {
      if (selectedPiece?.col === col && selectedPiece?.row === row) {
        deselectPiece();
      } else {
        selectPiece(col, row);
      }
    }
  };

  if (!game) {
    return <NewGameDialog onStart={newGame} loading={loading} />;
  }

  return (
    <div className="game-view">
      <div className="game-view__board">
        <Board
          board={game.board}
          selectedPiece={selectedPiece}
          legalMoves={legalMoves}
          humanSide={game.humanSide}
          currentTurn={game.currentTurn}
          onSquareClick={handleSquareClick}
        />
      </div>
      <div className="game-view__sidebar">
        <GameInfo
          currentTurn={game.currentTurn}
          humanSide={game.humanSide}
          capturedCount={game.capturedCount}
          aiThinking={aiThinking}
        />
        <button className="game-view__new-game-btn" onClick={resetGame}>
          New Game
        </button>
      </div>
      {error && <div className="game-view__error">{error}</div>}
      {game.phase === 'GAME_OVER' && (
        <GameOverDialog
          winner={game.winner}
          humanSide={game.humanSide}
          winReason={game.winReason}
          onPlayAgain={resetGame}
        />
      )}
    </div>
  );
}
