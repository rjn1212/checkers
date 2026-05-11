import type { BoardData, MoveData, Position, Player } from '../../types/game';
import type { MoveTrail } from '../../hooks/useGame';
import { Piece } from '../Piece/Piece';
import './Board.css';

interface BoardProps {
  board: BoardData;
  selectedPiece: Position | null;
  legalMoves: MoveData[];
  humanSide: Player;
  currentTurn: Player;
  lastHumanTrail: MoveTrail | null;
  lastAiTrail: MoveTrail | null;
  onSquareClick: (col: number, row: number) => void;
}

export function Board({
  board,
  selectedPiece,
  legalMoves,
  humanSide,
  currentTurn,
  lastHumanTrail,
  lastAiTrail,
  onSquareClick,
}: BoardProps) {
  const isDestination = (col: number, row: number) =>
    legalMoves.some((m) => m.to.col === col && m.to.row === row);

  const isSelected = (col: number, row: number) =>
    selectedPiece?.col === col && selectedPiece?.row === row;

  const getTrailClass = (col: number, row: number): string => {
    const classes: string[] = [];

    if (lastHumanTrail) {
      if (lastHumanTrail.from.col === col && lastHumanTrail.from.row === row) {
        classes.push('board__square--human-from');
      }
      if (lastHumanTrail.to.col === col && lastHumanTrail.to.row === row) {
        classes.push('board__square--human-to');
      }
      if (lastHumanTrail.captured.some((c) => c.col === col && c.row === row)) {
        classes.push('board__square--human-captured');
      }
    }

    if (lastAiTrail) {
      if (lastAiTrail.from.col === col && lastAiTrail.from.row === row) {
        classes.push('board__square--ai-from');
      }
      if (lastAiTrail.to.col === col && lastAiTrail.to.row === row) {
        classes.push('board__square--ai-to');
      }
      if (lastAiTrail.captured.some((c) => c.col === col && c.row === row)) {
        classes.push('board__square--ai-captured');
      }
    }

    return classes.join(' ');
  };

  const rows = [];
  for (let row = 0; row < 8; row++) {
    for (let col = 0; col < 8; col++) {
      const isDark = (col + row) % 2 === 1;
      const piece = board.pieces.find((p) => p.col === col && p.row === row);
      const selected = isSelected(col, row);
      const destination = isDestination(col, row);
      const trailClass = getTrailClass(col, row);
      const isClickable = isDark && (
        (piece && piece.owner === humanSide && piece.owner === currentTurn) || destination
      );

      rows.push(
        <div
          key={`${col}-${row}`}
          className={[
            'board__square',
            isDark ? 'board__square--dark' : 'board__square--light',
            selected ? 'board__square--selected' : '',
            destination ? 'board__square--destination' : '',
            isClickable ? 'board__square--clickable' : '',
            trailClass,
          ].join(' ')}
          onClick={() => isClickable && onSquareClick(col, row)}
        >
          {piece && <Piece piece={piece} />}
          {destination && !piece && <div className="board__move-dot" />}
        </div>
      );
    }
  }

  return <div className="board">{rows}</div>;
}
