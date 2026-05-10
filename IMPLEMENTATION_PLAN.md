# Checkers — Restructure Plan

## Context

The current Checkers game is a Java Swing desktop application (~1,200 lines, 7 classes). The game logic is solid — standard American Checkers with forced jumps, multi-capture chains, king promotion, and a minimax AI with alpha-beta pruning.

This plan restructures it into a **Spring Boot REST API + React web frontend**, matching the Stratego architecture pattern. The same backend will serve web, iPhone (React Native), and Android clients. The game will integrate with the game-platform portal via the platform contract.

## Current State

```
Checkers/
└── src/
    ├── Checker.java              # Piece model + Swing rendering
    ├── CheckerBoard.java         # Board state + Swing rendering
    ├── CheckerBoardComponent.java # Swing JComponent (UI only)
    ├── CheckerBoardViewer.java    # Swing JFrame entry point (UI only)
    ├── GameController.java        # Game state machine + Swing event handling
    ├── GameRules.java             # Pure game logic (moves, jumps, king promotion)
    └── AIPlayer.java              # Minimax AI with alpha-beta pruning
```

**What can be reused directly:**
- `GameRules.java` — move validation, jump sequences, forced jumps, king promotion, game over detection. Only dependency on `java.awt.Point` (replace with `Position` record).
- `AIPlayer.java` — minimax with alpha-beta pruning, evaluation function. No UI dependencies. Direct port.

**What needs refactoring:**
- `Checker.java` — strip `draw()` and `java.awt.Color`, keep data fields (position, player, king status). Becomes `Piece.java`.
- `CheckerBoard.java` — strip `draw()`, highlights, and size. Keep board state management (add/remove/move/deepCopy). Becomes `Board.java`.
- `GameController.java` — extract state machine logic from Swing event handling. Becomes `GameService.java`.

**What is deleted:**
- `CheckerBoardComponent.java` — Swing-only, replaced by React.
- `CheckerBoardViewer.java` — Swing-only, replaced by React.

---

## Target Architecture

```
┌──────────────────┐     REST API (JSON)     ┌──────────────────────┐
│  React Web App   │ ◄─────────────────────► │  Spring Boot Backend │
│  (Vite + TS)     │    /api/v1/*            │                      │
└──────────────────┘                         │  ┌────────────────┐  │
                                             │  │  GameController │  │
┌──────────────────┐                         │  └───────┬────────┘  │
│  React Native    │ ◄─── same API ────────► │  ┌───────▼────────┐  │
│  (future)        │                         │  │  GameService    │  │
└──────────────────┘                         │  └───────┬────────┘  │
                                             │  ┌───────▼────────┐  │
                                             │  │  GameEngine     │  │
                                             │  │  MoveValidator  │  │
                                             │  │  CombatResolver │  │
                                             │  └───────┬────────┘  │
                                             │  ┌───────▼────────┐  │
                                             │  │  AI Engine      │  │
                                             │  │  Easy / Hard    │  │
                                             │  └────────────────┘  │
                                             └──────────────────────┘
```

**Key decisions (matching Stratego):**
- Server-authoritative: all game logic on server, clients are pure renderers
- Synchronous AI: AI move returned in same HTTP response as human's move
- In-memory storage: `ConcurrentHashMap<String, Game>` — no database for v1
- No fog of war (both players see all pieces — simpler than Stratego)
- No setup phase (pieces start in fixed positions — simpler than Stratego)

---

## Target Project Structure

```
Checkers/
├── pom.xml                              (parent POM, modules: backend + frontend)
├── IMPLEMENTATION_PLAN.md
├── Checkers_Rules.md
├── checkers-backend/
│   ├── pom.xml
│   └── src/main/java/com/gitgames/checkers/
│       ├── CheckersApplication.java
│       ├── config/
│       │   ├── WebConfig.java           (CORS, SPA forwarding)
│       │   └── JacksonConfig.java
│       ├── controller/
│       │   ├── GameController.java
│       │   └── dto/
│       │       ├── CreateGameRequest.java
│       │       ├── MakeMoveRequest.java
│       │       ├── GameStateResponse.java
│       │       ├── MoveResponse.java
│       │       ├── LegalMovesResponse.java
│       │       ├── PieceDto.java
│       │       └── MoveDto.java
│       ├── model/
│       │   ├── Game.java                (aggregate root)
│       │   ├── Board.java               (8x8 Piece grid)
│       │   ├── Piece.java               (id, player, position, king status)
│       │   ├── Player.java              (enum: BLACK, RED)
│       │   ├── GamePhase.java           (enum: PLAYING, GAME_OVER)
│       │   ├── Position.java            (record: col, row)
│       │   ├── Move.java                (from, steps, captured, becomesKing)
│       │   ├── MoveRecord.java          (for move history)
│       │   └── AiDifficulty.java        (enum: EASY, HARD)
│       ├── engine/
│       │   ├── GameEngine.java          (orchestrates turns)
│       │   ├── MoveValidator.java       (all movement rules)
│       │   └── WinConditionChecker.java
│       ├── ai/
│       │   ├── AiStrategy.java          (interface)
│       │   ├── EasyAiStrategy.java      (greedy captures)
│       │   └── HardAiStrategy.java      (minimax + alpha-beta)
│       ├── service/
│       │   ├── GameService.java
│       │   └── GameRepository.java      (ConcurrentHashMap wrapper)
│       └── platform/
│           └── PlatformController.java  (GET /platform/health)
├── checkers-frontend/
│   ├── pom.xml                          (frontend-maven-plugin)
│   ├── package.json
│   ├── vite.config.ts                   (proxy /api to :8080)
│   ├── tsconfig.json
│   └── src/
│       ├── main.tsx
│       ├── App.tsx
│       ├── api/gameApi.ts               (fetch wrapper)
│       ├── types/game.ts                (mirrors API types)
│       ├── hooks/useGame.ts             (useReducer state management)
│       └── components/
│           ├── Board/Board.tsx + .css    (CSS Grid 8x8)
│           ├── Piece/Piece.tsx + .css
│           ├── Game/GameView.tsx         (main play screen)
│           ├── Game/GameInfo.tsx         (turn, captured pieces)
│           ├── Game/MoveHighlight.tsx    (valid move indicators)
│           └── Common/
│               ├── NewGameDialog.tsx     (side + difficulty)
│               └── GameOverDialog.tsx
├── src/                                 (old Swing code — delete after migration)
└── .gitignore
```

---

## Domain Model

### Player Enum

Replaces the `int playerNumber` (0/1) convention.

```java
public enum Player {
    BLACK,  // player 0 in the original, moves down (rows 0-2 start), promotes at row 7
    RED;    // player 1 in the original, moves up (rows 5-7 start), promotes at row 0

    public Player opponent() {
        return this == BLACK ? RED : BLACK;
    }
}
```

BLACK always goes first (standard American Checkers).

### Position Record

Replaces `java.awt.Point` and the `int x, int y` pairs scattered through the code.

```java
public record Position(int col, int row) {
    public boolean isValid() {
        return col >= 0 && col < 8 && row >= 0 && row < 8;
    }

    public boolean isPlayableSquare() {
        return (col + row) % 2 == 1;
    }
}
```

### Piece

Stripped from `Checker.java`. No rendering, no `java.awt.Color`, no `size`.

```java
public class Piece implements Cloneable {
    private final String id;
    private final Player owner;
    private int col;
    private int row;
    private boolean king;

    // constructor, getters, setters, clone()
}
```

### Board

Stripped from `CheckerBoard.java`. No `draw()`, no highlights, no `size`.

```java
public class Board {
    private final Piece[][] grid = new Piece[8][8];
    private final List<Piece> pieces = new ArrayList<>();

    public void addPiece(Piece piece) { ... }
    public void removePiece(Piece piece) { ... }
    public void movePiece(Piece piece, int col, int row) { ... }
    public Piece getPieceAt(int col, int row) { ... }
    public List<Piece> getPieces() { ... }
    public List<Piece> getPiecesForPlayer(Player player) { ... }
    public Board deepCopy() { ... }
}
```

### Game (Aggregate Root)

```java
public class Game {
    private final String id;
    private final Board board;
    private Player currentTurn;
    private GamePhase phase;
    private Player winner;
    private String winReason;
    private final AiDifficulty aiDifficulty;
    private final Player humanSide;
    private final List<MoveRecord> moveHistory;
    private final Map<Player, String> platformUserIds;  // for platform auth
    private final Map<Player, Integer> capturedCount;    // pieces captured by each player
    private final Instant createdAt;
    private Instant lastActivityAt;
}
```

### Move

Ported from `GameRules.Move`. Same structure, uses `Position` instead of `int[]`.

```java
public class Move {
    private final Position from;
    private final List<Position> steps;       // each step in the path
    private final List<Position> captured;    // positions of captured pieces
    private final boolean becomesKing;

    public Position getTo() { return steps.get(steps.size() - 1); }
    public boolean isJump() { return !captured.isEmpty(); }
}
```

---

## REST API

### Game Lifecycle

```
POST   /api/v1/games                Create game
GET    /api/v1/games/{id}           Get game state
DELETE /api/v1/games/{id}           Abandon game
```

#### POST /api/v1/games

Request:
```json
{
  "aiDifficulty": "EASY",
  "playerSide": "BLACK"
}
```

Response `201`:
```json
{
  "gameId": "abc-123",
  "phase": "PLAYING",
  "currentTurn": "BLACK",
  "humanSide": "BLACK",
  "board": {
    "pieces": [
      { "id": "B0", "owner": "BLACK", "col": 1, "row": 0, "king": false },
      { "id": "B1", "owner": "BLACK", "col": 3, "row": 0, "king": false },
      ...
    ]
  },
  "capturedCount": { "BLACK": 0, "RED": 0 }
}
```

If `playerSide` is `RED`, the server immediately makes BLACK's (AI's) first move and returns the resulting board state.

#### GET /api/v1/games/{id}

Response `200`: same shape as the create response, plus `moveHistory` and `winner`/`winReason` if game is over.

### Moves

```
GET  /api/v1/games/{id}/moves                    All legal moves for current player
GET  /api/v1/games/{id}/moves?col={c}&row={r}    Legal moves for piece at position
POST /api/v1/games/{id}/moves                    Make a move
```

#### GET /api/v1/games/{id}/moves?col=1&row=2

Response `200`:
```json
{
  "moves": [
    {
      "from": { "col": 1, "row": 2 },
      "to": { "col": 0, "row": 3 },
      "steps": [{ "col": 0, "row": 3 }],
      "captured": [],
      "becomesKing": false
    },
    {
      "from": { "col": 1, "row": 2 },
      "to": { "col": 3, "row": 4 },
      "steps": [{ "col": 3, "row": 4 }],
      "captured": [{ "col": 2, "row": 3 }],
      "becomesKing": false
    }
  ],
  "mustJump": true
}
```

The `mustJump` flag tells the client whether all returned moves are forced jumps.

#### POST /api/v1/games/{id}/moves

Request:
```json
{
  "from": { "col": 1, "row": 2 },
  "to": { "col": 3, "row": 4 }
}
```

The server finds the matching move from the legal moves list (including multi-jump paths that end at `to`). If multiple paths lead to the same destination, the server picks the one with maximum captures.

Response `200`:
```json
{
  "humanMove": {
    "from": { "col": 1, "row": 2 },
    "to": { "col": 3, "row": 4 },
    "captured": [{ "col": 2, "row": 3 }],
    "becomesKing": false
  },
  "aiMove": {
    "from": { "col": 4, "row": 5 },
    "to": { "col": 2, "row": 3 },
    "captured": [],
    "becomesKing": false
  },
  "board": { ... },
  "phase": "PLAYING",
  "currentTurn": "BLACK",
  "capturedCount": { "BLACK": 1, "RED": 0 },
  "winner": null,
  "winReason": null
}
```

If the game ends after the human's move, `aiMove` is null, `phase` is `GAME_OVER`, and `winner`/`winReason` are populated. If the game ends after the AI's move, `aiMove` is present and `winner` is the AI.

### Error Responses

```json
{ "error": "NOT_YOUR_TURN", "message": "It is not your turn" }
{ "error": "INVALID_MOVE", "message": "No legal move from (1,2) to (5,6)" }
{ "error": "GAME_OVER", "message": "Game is already over" }
{ "error": "GAME_NOT_FOUND", "message": "No game with id abc-123" }
```

---

## Engine Design

### MoveValidator

Ported from `GameRules.java`. All methods become instance methods on a `MoveValidator` class or stay as static utilities in a `MoveRules` class.

Key logic preserved from the original:
- Direction checking based on player + king status
- Simple moves (1 square diagonal)
- Jump sequences (recursive multi-capture with `Set<Position> alreadyCaptured`)
- Forced jump rule (if jumps exist, simple moves are blocked)
- King promotion (reaching opposite row, ends turn mid-chain)
- Game over detection (no legal moves = loss)

The original `GameRules.java` is ~230 lines of clean logic. The port mostly changes `int x, int y` to `Position`, `Checker` to `Piece`, `CheckerBoard` to `Board`, and `java.awt.Point` to `Position`.

### GameEngine

Orchestrates a single turn:
1. Validate the move is legal
2. Apply the move to the board
3. Check for win condition
4. If game continues and opponent is AI, compute and apply AI move
5. Check for win condition again
6. Return the result

### AiStrategy Interface

```java
public interface AiStrategy {
    Move selectMove(Board board, Player player);
}
```

Two implementations ported from `AIPlayer.java`:

- `EasyAiStrategy` — greedy: pick the move with maximum captures, random tiebreaker
- `HardAiStrategy` — minimax with alpha-beta pruning, depth 8, evaluation function considering material (regular=2, king=5), center control, advancement, and back row protection

---

## Implementation Phases

### Phase 1: Project Skeleton + Domain Model

**Goal:** Maven multi-module build, all model classes, Spring Boot app running, React skeleton.

- [ ] Create parent `pom.xml` with modules `checkers-backend` and `checkers-frontend`
- [ ] Create `checkers-backend/pom.xml` (Spring Boot 3.3.5, Java 21)
- [ ] Create `checkers-frontend/pom.xml` (frontend-maven-plugin, Node 20)
- [ ] Model classes:
  - [ ] `Player.java` (enum: BLACK, RED with `opponent()`)
  - [ ] `Position.java` (record: col, row with `isValid()`, `isPlayableSquare()`)
  - [ ] `Piece.java` (id, owner, col, row, king — stripped from Checker.java)
  - [ ] `Board.java` (8x8 grid — stripped from CheckerBoard.java)
  - [ ] `GamePhase.java` (enum: PLAYING, GAME_OVER)
  - [ ] `AiDifficulty.java` (enum: EASY, HARD)
  - [ ] `Move.java` (from, steps, captured, becomesKing — ported from GameRules.Move)
  - [ ] `MoveRecord.java` (for history)
  - [ ] `Game.java` (aggregate root)
- [ ] `CheckersApplication.java` (Spring Boot main)
- [ ] `WebConfig.java` (CORS with configurable origins, SPA routing)
- [ ] `JacksonConfig.java` (ObjectMapper with JavaTimeModule)
- [ ] Unit tests for Position, Board, Piece
- [ ] React Vite project: `npm create vite@latest` with React + TypeScript
- [ ] Verify: `mvn clean package` builds both modules, Spring Boot serves React

### Phase 2: Game Engine + Move Validation

**Goal:** All game rules working and tested. Port from GameRules.java.

- [ ] `MoveValidator.java` — port from `GameRules`:
  - [ ] `getLegalMoves(board, player)` — forced jump rule
  - [ ] `getMovesForPiece(board, piece)` — per-piece moves
  - [ ] `getSimpleMoves()` — single diagonal moves
  - [ ] `getJumpSequences()` — recursive multi-capture chains
  - [ ] `findMoveByDestination()` — match move by endpoint
- [ ] `WinConditionChecker.java` — `isGameOver(board, player)`
- [ ] `GameEngine.java` — orchestrate: validate → apply → check win
- [ ] Unit tests:
  - [ ] Simple moves (all directions, king vs regular)
  - [ ] Single jump captures
  - [ ] Multi-jump chains
  - [ ] Forced jump rule
  - [ ] King promotion (including promotion-ends-chain)
  - [ ] Win conditions (no pieces, no legal moves)
  - [ ] Edge cases (back row, corners)

### Phase 3: AI Engine

**Goal:** Both AI difficulties working and tested. Port from AIPlayer.java.

- [ ] `AiStrategy.java` interface
- [ ] `EasyAiStrategy.java` — greedy capture selection, random tiebreaker
- [ ] `HardAiStrategy.java` — minimax with alpha-beta pruning:
  - [ ] Depth 8 search
  - [ ] Evaluation: material (regular=2, king=5), center control (+1 for cols 2-5), advancement bonus, back row protection (+1)
  - [ ] Random tiebreaker among equally-scored moves
- [ ] Unit tests:
  - [ ] Easy AI picks captures over simple moves
  - [ ] Hard AI avoids obvious traps
  - [ ] Both AIs return valid moves

### Phase 4: REST API

**Goal:** Full API layer with service, controller, DTOs, and repository.

- [ ] `GameRepository.java` — `ConcurrentHashMap<String, Game>` wrapper
- [ ] `GameService.java`:
  - [ ] `createGame(difficulty, playerSide)` — init board, if AI goes first make AI move
  - [ ] `getGame(id)` — return game state
  - [ ] `getLegalMoves(id, col, row)` — optional position filter
  - [ ] `makeMove(id, from, to)` — validate, apply, AI response, return result
  - [ ] `abandonGame(id)` — remove from map
- [ ] DTOs:
  - [ ] `CreateGameRequest`, `MakeMoveRequest`
  - [ ] `GameStateResponse`, `MoveResponse`, `LegalMovesResponse`
  - [ ] `PieceDto`, `MoveDto`, `PositionDto`
- [ ] `GameController.java` — REST endpoints mapped to GameService
- [ ] Error handling: `GameNotFoundException`, `InvalidMoveException`, `@ControllerAdvice`
- [ ] Integration tests: full API flow via MockMvc

### Phase 5: Frontend

**Goal:** Playable game in the browser.

- [ ] TypeScript types (`src/types/game.ts`) — mirror API DTOs
- [ ] API client (`src/api/gameApi.ts`) — fetch wrapper with injectable headers
- [ ] `useGame` hook — game state management with useReducer
- [ ] Components:
  - [ ] `Board.tsx` — CSS Grid 8x8, alternating colors, click handling
  - [ ] `Piece.tsx` — circle with player color, king indicator
  - [ ] `MoveHighlight.tsx` — green overlay on valid destination squares
  - [ ] `GameView.tsx` — main play screen (board + info panel)
  - [ ] `GameInfo.tsx` — current turn, captured pieces count
  - [ ] `NewGameDialog.tsx` — side selection + difficulty
  - [ ] `GameOverDialog.tsx` — win/loss message, play again
- [ ] Game flow:
  - [ ] New game → POST /api/v1/games → render board
  - [ ] Click piece → GET /api/v1/games/{id}/moves?col&row → highlight destinations
  - [ ] Click destination → POST /api/v1/games/{id}/moves → animate human + AI moves
  - [ ] Game over → show dialog
- [ ] Styling: dark theme matching platform aesthetic

### Phase 6: Platform Integration

**Goal:** Checkers integrates with the game-platform portal.

- [ ] `PlatformController.java` — `GET /platform/health`
- [ ] `PlatformAuthFilter.java` — JWT verification (optional, anonymous access works)
- [ ] Frontend: read `platformToken` from URL, store in sessionStorage, send on API calls
- [ ] Frontend: "Back to Platform" link using `returnUrl` param
- [ ] CORS: configurable via `CORS_ALLOWED_ORIGINS` env var
- [ ] `Dockerfile` — multi-stage (eclipse-temurin:21-jdk → eclipse-temurin:21-jre)
- [ ] Update game-platform `games.json` with Checkers entry

### Phase 7: Polish

**Goal:** Smooth, polished experience.

- [ ] AI move animation (brief highlight before applying)
- [ ] Move animations (piece slides to new position)
- [ ] Capture animation (captured piece fades out)
- [ ] Forced jump indicator (highlight pieces that must jump)
- [ ] Captured pieces tray (show captured pieces beside the board)
- [ ] Move history panel
- [ ] Responsive layout (mobile-friendly board sizing)
- [ ] Keyboard navigation
- [ ] Error handling and loading states
- [ ] Surrender button

---

## Checkers Rules Reference

Standard American Checkers (8x8 board):

- **Board:** 8x8, pieces only on dark squares
- **Pieces:** 12 per player, starting in first 3 rows
- **Movement:** diagonal, forward only (one square)
- **Kings:** reach opposite row, can move diagonally in all 4 directions
- **Captures:** jump over adjacent opponent piece to empty square beyond. Mandatory — if a jump is available, you must take it.
- **Multi-jumps:** after a capture, if another jump is available from the landing square, you must continue jumping (same turn)
- **King promotion during jump:** if a piece reaches the king row during a multi-jump, it is promoted and the turn ends (no further jumps in that turn)
- **Win:** opponent has no legal moves (all pieces captured or all pieces blocked)
- **BLACK always moves first**

---

## Migration Notes

### Mapping from old code to new code

| Old (Swing) | New (Spring Boot + React) |
|---|---|
| `Checker.java` | `model/Piece.java` (strip draw, Color, size) |
| `CheckerBoard.java` | `model/Board.java` (strip draw, highlights) |
| `GameRules.java` | `engine/MoveValidator.java` + `engine/WinConditionChecker.java` |
| `GameRules.Move` | `model/Move.java` |
| `AIPlayer.java` | `ai/EasyAiStrategy.java` + `ai/HardAiStrategy.java` |
| `GameController.java` | `service/GameService.java` + `controller/GameController.java` |
| `CheckerBoardComponent.java` | `frontend/src/components/Board/Board.tsx` |
| `CheckerBoardViewer.java` | `frontend/src/App.tsx` |
| `int playerNumber` (0/1) | `Player` enum (BLACK/RED) |
| `java.awt.Point` | `Position` record |
| `run.sh` | `mvn clean package && java -jar` |

### What to watch for during port

1. **Coordinate convention:** Original uses `x` = column, `y` = row (x increases right, y increases down). The new `Position(col, row)` preserves this. Don't swap them.

2. **Player direction:** BLACK (player 0) moves down (increasing row). RED (player 1) moves up (decreasing row). This is baked into `getDirections()` in `GameRules.java` — port carefully.

3. **Jump recursion:** The `getJumpSequences()` method tracks `alreadyCaptured` as a `Set<Point>` to prevent re-jumping the same piece. Port this as `Set<Position>`.

4. **King promotion ends chain:** When a non-king piece reaches the promotion row during a multi-jump, it becomes a king and the turn ends immediately. This is a specific rules choice — preserve it.

5. **Forced jump rule:** If any piece can jump, all non-jump moves are illegal. This is enforced in `getLegalMoves()` — the method returns only jumps when jumps exist.

6. **Deep copy for AI:** The AI's minimax calls `applyMoveOnCopy()` which clones the entire board. This is critical for search — the AI must not mutate the real game state. `Board.deepCopy()` + `Piece.clone()` must work correctly.

7. **Delete old `src/` directory** after the migration is complete and verified.
