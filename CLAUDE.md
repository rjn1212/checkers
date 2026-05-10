# Checkers

Standard American Checkers restructured as a web app. Java 21 / Spring Boot 3.3.5 backend, React 18 / TypeScript / Vite frontend. Maven multi-module build.

See `IMPLEMENTATION_PLAN.md` for the full build plan.

## Build & test

```bash
# Build everything (backend + frontend)
mvn clean package

# Run backend (serves frontend from static resources)
cd checkers-backend && mvn spring-boot:run

# Run frontend dev server (proxies /api to :8080)
cd checkers-frontend && npm run dev

# Run tests
mvn test
```

- Backend: http://localhost:8080
- Frontend dev: http://localhost:5173 (proxied to backend)

## Project structure

- `checkers-backend/` — Spring Boot REST API, all game logic, AI
- `checkers-frontend/` — React SPA, bundled into backend's `static/` by Maven
- `src/` — old Swing code (reference only, delete after migration is verified)

## Porting from Swing

The old Swing code in `src/` is the reference implementation. The game logic in `GameRules.java` and `AIPlayer.java` is clean and should be ported directly — don't rewrite the algorithms, just adapt the types.

Key type mappings:
- `int playerNumber` (0/1) → `Player` enum (BLACK/RED)
- `java.awt.Point` → `Position` record
- `Checker` → `Piece` (strip rendering)
- `CheckerBoard` → `Board` (strip rendering)

## Platform integration

This game will be hosted on a game-platform portal. The platform is a separate Next.js web shell that provides auth, a game catalog, and score tracking. Games integrate via a contract — they are NOT part of the platform monorepo.

### What this means for development

**1. Don't hardcode which Player is human or AI in the engine layer.**

`Player.BLACK` and `Player.RED` are game roles, not identity types. The engine (`MoveValidator`, `WinConditionChecker`) should work with `Player` values without knowing or caring which is human. The decision about "which side is AI" belongs in `GameService`, not the engine.

This matters because multiplayer (human vs human) will be added later.

**2. The Game model should support optional user identity.**

When the platform sends an authenticated user, the game needs to associate that user with a player slot:

```java
private final Map<Player, String> platformUserIds = new EnumMap<>(Player.class);
```

For single-player, only the human side has a user ID (or neither, if anonymous). For future multiplayer, both slots have user IDs. Game logic doesn't change.

**3. Make CORS origins configurable.**

Use an environment variable, not hardcoded values:

```java
@Value("${cors.allowed-origins:http://localhost:5173}")
private String allowedOrigins;
```

**4. Structure the frontend API client to accept auth headers.**

`gameApi.ts` should use a pattern where headers can be extended — e.g., a shared `getHeaders()` function or a fetch wrapper that merges default + auth headers. Don't build token handling now, just make the API client extensible.

**5. The `/api/v1/` namespace is yours.**

Platform integration endpoints go under `/platform/` (e.g., `GET /platform/health`). No conflict with game routes.

**6. The synchronous AI response is fine but should be conditional.**

The move endpoint returns the AI's move in the same response. Keep the "should AI respond?" decision in `GameService`, not the controller. When multiplayer is added, the endpoint returns only the human's move.

### What NOT to build yet

- No `/platform/health` endpoint (added in Phase 6)
- No JWT verification filter (added in Phase 6)
- No Dockerfile (added in Phase 6)
- No `@gitgames/platform-sdk` dependency in the frontend (added in Phase 6 — the SDK is built in the game-platform repo and published to npm)
- No `games.json` registration (added in Phase 6 — add an entry to the game-platform repo's `games.json` with the game's Railway URL, health endpoint, and feature flags)

Focus on building a complete, working single-player game first. Platform integration is additive.
