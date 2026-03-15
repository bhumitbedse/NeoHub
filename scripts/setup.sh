#!/bin/bash
# ─────────────────────────────────────────────────────
#  NeoHub — Post-Create Setup Script
#  Runs automatically after devcontainer is created
# ─────────────────────────────────────────────────────

set -e

GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}"
echo "  _   _            _   _       _     "
echo " | \ | | ___  ___ | | | |_   _| |__ "
echo " |  \| |/ _ \/ _ \| |_| | | | | '_ \\"
echo " | |\  |  __/ (_) |  _  | |_| | |_) |"
echo " |_| \_|\___|\___/|_| |_|\__,_|_.__/ "
echo -e "${NC}"
echo -e "${GREEN}Setting up NeoHub development environment...${NC}"
echo ""

# ── Install Angular CLI ──────────────────────────
echo -e "${YELLOW}[1/5] Installing Angular CLI...${NC}"
npm install -g @angular/cli@17 --silent
echo "      ✅ Angular CLI installed"

# ── Install frontend dependencies ───────────────
if [ -f "/workspace/frontend/package.json" ]; then
    echo -e "${YELLOW}[2/5] Installing frontend dependencies...${NC}"
    cd /workspace/frontend && npm install --silent
    echo "      ✅ Frontend dependencies installed"
else
    echo -e "${YELLOW}[2/5] No frontend package.json yet — skipping${NC}"
fi

# ── Download Go dependencies ─────────────────────
echo -e "${YELLOW}[3/5] Downloading Go modules...${NC}"
if [ -f "/workspace/cli/go.mod" ]; then
    cd /workspace/cli && go mod download 2>/dev/null || true
    echo "      ✅ Go modules downloaded"
else
    echo "      ⏭  No go.mod yet — skipping"
fi

# ── Wait for Postgres and run checks ────────────
echo -e "${YELLOW}[4/5] Checking database connection...${NC}"
MAX_RETRIES=30
COUNT=0
until pg_isready -h postgres -U neohub -d neohub > /dev/null 2>&1; do
    COUNT=$((COUNT+1))
    if [ $COUNT -ge $MAX_RETRIES ]; then
        echo "      ⚠️  PostgreSQL not ready after ${MAX_RETRIES}s — start it with: docker compose up postgres"
        break
    fi
    sleep 1
done

if pg_isready -h postgres -U neohub -d neohub > /dev/null 2>&1; then
    echo "      ✅ PostgreSQL is ready"
fi

# ── Print useful commands ────────────────────────
echo -e "${YELLOW}[5/5] Setup complete!${NC}"
echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}  NeoHub is ready to develop!${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo "  Start all services:       docker compose up"
echo "  Start just DB + Redis:    docker compose up postgres redis"
echo ""
echo "  Frontend (Angular):       cd frontend && ng serve"
echo "  API Gateway:              cd backend/api-gateway && mvn spring-boot:run"
echo "  CLI:                      cd cli && go run main.go"
echo ""
echo "  pgAdmin UI:               http://localhost:8091"
echo "  Redis Commander:          http://localhost:8090"
echo "  Swagger UI:               http://localhost:8080/swagger-ui"
echo "  Mailpit (email):          http://localhost:8025"
echo ""
echo "  GitHub OAuth setup:       See README.md → Local Setup"
echo ""
