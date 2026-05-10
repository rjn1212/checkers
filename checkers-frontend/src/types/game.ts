export type Player = 'BLACK' | 'RED';
export type GamePhase = 'PLAYING' | 'GAME_OVER';
export type AiDifficulty = 'EASY' | 'HARD';

export interface Position {
  col: number;
  row: number;
}

export interface PieceData {
  id: string;
  owner: Player;
  col: number;
  row: number;
  king: boolean;
}

export interface BoardData {
  pieces: PieceData[];
}

export interface MoveData {
  from: Position;
  to: Position;
  steps: Position[];
  captured: Position[];
  becomesKing: boolean;
}

export interface GameState {
  gameId: string;
  phase: GamePhase;
  currentTurn: Player;
  humanSide: Player;
  board: BoardData;
  capturedCount: Record<Player, number>;
  winner: Player | null;
  winReason: string | null;
}

export interface MoveResponse {
  humanMove: MoveData;
  aiMove: MoveData | null;
  board: BoardData;
  phase: GamePhase;
  currentTurn: Player;
  capturedCount: Record<Player, number>;
  winner: Player | null;
  winReason: string | null;
}

export interface LegalMovesResponse {
  moves: MoveData[];
  mustJump: boolean;
}

export interface CreateGameRequest {
  aiDifficulty: AiDifficulty;
  playerSide: Player;
}

export interface MakeMoveRequest {
  from: Position;
  to: Position;
}
