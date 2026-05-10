import type { Player } from '../../types/game';
import './GameInfo.css';

interface GameInfoProps {
  currentTurn: Player;
  humanSide: Player;
  capturedCount: Record<Player, number>;
  aiThinking: boolean;
}

export function GameInfo({ currentTurn, humanSide, capturedCount, aiThinking }: GameInfoProps) {
  const isHumanTurn = currentTurn === humanSide;

  return (
    <div className="game-info">
      <div className="game-info__turn">
        {aiThinking ? (
          <span className="game-info__thinking">AI is thinking...</span>
        ) : (
          <span>{isHumanTurn ? 'Your turn' : "Opponent's turn"}</span>
        )}
      </div>
      <div className="game-info__side">
        Playing as: <strong>{humanSide}</strong>
      </div>
      <div className="game-info__captured">
        <div>Your captures: {capturedCount[humanSide]}</div>
        <div>AI captures: {capturedCount[humanSide === 'BLACK' ? 'RED' : 'BLACK']}</div>
      </div>
    </div>
  );
}
