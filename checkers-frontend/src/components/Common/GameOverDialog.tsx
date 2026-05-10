import type { Player } from '../../types/game';
import './GameOverDialog.css';

interface GameOverDialogProps {
  winner: Player | null;
  humanSide: Player;
  winReason: string | null;
  onPlayAgain: () => void;
}

export function GameOverDialog({ winner, humanSide, winReason, onPlayAgain }: GameOverDialogProps) {
  const isWin = winner === humanSide;

  return (
    <div className="game-over-overlay">
      <div className="game-over-dialog">
        <h2 className={`game-over-dialog__title ${isWin ? 'win' : 'loss'}`}>
          {isWin ? 'You Win!' : 'You Lose'}
        </h2>
        {winReason && <p className="game-over-dialog__reason">{winReason}</p>}
        <button className="game-over-dialog__btn" onClick={onPlayAgain}>
          Play Again
        </button>
      </div>
    </div>
  );
}
