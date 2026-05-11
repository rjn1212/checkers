import { PlatformClient } from '@gitgames/platform-sdk';
import type {
  CreateGameRequest,
  GameState,
  LegalMovesResponse,
  MakeMoveRequest,
  MoveResponse,
} from '../types/game';

export const platform = new PlatformClient();

const BASE_URL = '/api/v1/games';

function getHeaders(): Record<string, string> {
  return {
    'Content-Type': 'application/json',
    ...platform.getAuthHeaders(),
  };
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: response.statusText }));
    throw new Error(error.message || response.statusText);
  }
  return response.json();
}

export async function createGame(request: CreateGameRequest): Promise<GameState> {
  const response = await fetch(BASE_URL, {
    method: 'POST',
    headers: getHeaders(),
    body: JSON.stringify(request),
  });
  return handleResponse<GameState>(response);
}

export async function getGame(gameId: string): Promise<GameState> {
  const response = await fetch(`${BASE_URL}/${gameId}`, {
    headers: getHeaders(),
  });
  return handleResponse<GameState>(response);
}

export async function getLegalMoves(
  gameId: string,
  col?: number,
  row?: number
): Promise<LegalMovesResponse> {
  let url = `${BASE_URL}/${gameId}/moves`;
  if (col !== undefined && row !== undefined) {
    url += `?col=${col}&row=${row}`;
  }
  const response = await fetch(url, {
    headers: getHeaders(),
  });
  return handleResponse<LegalMovesResponse>(response);
}

export async function makeMove(
  gameId: string,
  request: MakeMoveRequest
): Promise<MoveResponse> {
  const response = await fetch(`${BASE_URL}/${gameId}/moves`, {
    method: 'POST',
    headers: getHeaders(),
    body: JSON.stringify(request),
  });
  return handleResponse<MoveResponse>(response);
}

export async function abandonGame(gameId: string): Promise<void> {
  await fetch(`${BASE_URL}/${gameId}`, {
    method: 'DELETE',
    headers: getHeaders(),
  });
}
