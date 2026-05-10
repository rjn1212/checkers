import type { PieceData } from '../../types/game';
import './Piece.css';

interface PieceProps {
  piece: PieceData;
}

export function Piece({ piece }: PieceProps) {
  return (
    <div className={`piece piece--${piece.owner.toLowerCase()}`}>
      <div className="piece__disc">
        {piece.king && <div className="piece__crown">&#9813;</div>}
      </div>
    </div>
  );
}
