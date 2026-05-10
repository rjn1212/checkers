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

  return (
    <div className="new-game-dialog">
      <h1 className="new-game-dialog__title">Checkers</h1>
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
    </div>
  );
}
