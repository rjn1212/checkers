#!/usr/bin/env bash
ROOT="$(cd "$(dirname "$0")" && pwd)"
BACKEND_DIR="$ROOT/checkers-backend"
FRONTEND_DIR="$ROOT/checkers-frontend"

BACKEND_PORT="${BACKEND_PORT:-8082}"
FRONTEND_PORT="${FRONTEND_PORT:-5175}"

echo "==> Starting Checkers"
echo "    Frontend: http://localhost:$FRONTEND_PORT"
echo "    Backend:  http://localhost:$BACKEND_PORT"
echo "    Ctrl+C to stop."
echo ""

# Clear ports
lsof -ti:"$BACKEND_PORT" | xargs kill -9 2>/dev/null || true
lsof -ti:"$FRONTEND_PORT" | xargs kill -9 2>/dev/null || true
sleep 1

# Start backend
cd "$BACKEND_DIR"
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Dserver.port=$BACKEND_PORT" &
BACKEND_PID=$!

# Start frontend
cd "$FRONTEND_DIR"
npx vite --port "$FRONTEND_PORT" &
FRONTEND_PID=$!

cleanup() {
  echo ""
  echo "Stopping..."
  kill "$BACKEND_PID" "$FRONTEND_PID" 2>/dev/null || true
  exit 0
}
trap cleanup INT TERM
wait
