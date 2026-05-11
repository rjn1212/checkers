import { useState } from 'react';
import type { AiDifficulty, Player } from '../../types/game';
import './NewGameDialog.css';

interface NewGameDialogProps {
  onStart: (difficulty: AiDifficulty, side: Player) => void;
  loading: boolean;
}

export function NewGameDialog({ onStart, loading }: NewGameDialogProps) {
  const [difficulty, setDifficulty] = useState<AiDifficulty>('EASY');
  const [side, setSide] = useState<Player>('BLACK');
  const [showRules, setShowRules] = useState(false);

  return (
    <div className="new-game-dialog">
      <h1 className="new-game-dialog__title">Checkers</h1>
      {showRules && (
        <div className="new-game-dialog__rules">
          <h2>How to Play</h2>
          <ul>
            <li>Played on the dark squares of an 8x8 board. Each player starts with 12 pieces.</li>
            <li><strong>Movement:</strong> Pieces move one square diagonally forward.</li>
            <li><strong>Capturing:</strong> Jump over an adjacent opponent piece to an empty square beyond it. The jumped piece is removed.</li>
            <li><strong>Forced jumps:</strong> If a capture is available, you <em>must</em> take it.</li>
            <li><strong>Multi-jumps:</strong> If another capture is available after landing, you must continue jumping in the same turn.</li>
            <li><strong>Kings:</strong> When a piece reaches the far row, it becomes a king and can move diagonally in all four directions.</li>
            <li><strong>Promotion during a jump:</strong> If a piece becomes a king mid-chain, the turn ends immediately.</li>
            <li><strong>Winning:</strong> You win when your opponent has no legal moves — either all their pieces are captured or all are blocked.</li>
          </ul>
          <button className="new-game-dialog__rules-close" onClick={() => setShowRules(false)}>
            Got it
          </button>
        </div>
      )}
      {!showRules && (
        <>
        <button className="new-game-dialog__rules-link" onClick={() => setShowRules(true)}>
          How to Play
        </button>
      <div className="new-game-dialog__options">
        <div className="new-game-dialog__group">
          <label>Difficulty</label>
          <div className="new-game-dialog__buttons">
            <button
              className={difficulty === 'EASY' ? 'active' : ''}
              onClick={() => setDifficulty('EASY')}
            >
              Easy
            </button>
            <button
              className={difficulty === 'HARD' ? 'active' : ''}
              onClick={() => setDifficulty('HARD')}
            >
              Hard
            </button>
          </div>
        </div>
        <div className="new-game-dialog__group">
          <label>Play as</label>
          <div className="new-game-dialog__buttons">
            <button
              className={side === 'BLACK' ? 'active' : ''}
              onClick={() => setSide('BLACK')}
            >
              Black (first)
            </button>
            <button
              className={side === 'RED' ? 'active' : ''}
              onClick={() => setSide('RED')}
            >
              Red (second)
            </button>
          </div>
        </div>
      </div>
      <button
        className="new-game-dialog__start"
        onClick={() => onStart(difficulty, side)}
        disabled={loading}
      >
        {loading ? 'Starting...' : 'Start Game'}
      </button>
      </>
      )}
    </div>
  );
}
