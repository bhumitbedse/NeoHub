# NeoHub

> The Neovim plugin registry with a CLI that actually installs plugins for you.

```bash
neostore install telescope
# Detects lazy.nvim, injects correct snippet, syncs — done.
```

---

## Local Development Setup (WSL2)

### Prerequisites

Install these in WSL2:

```bash
# Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
newgrp docker

# Verify
docker --version
docker compose version
```

Install **VS Code** on Windows with the **Remote - WSL** and **Dev Containers** extensions.

---

### Option A — DevContainer (Recommended)

Everything runs inside a container. Java, Node, Go all pre-installed.

```bash
# 1. Clone the repo
git clone https://github.com/bhumitbedse/neohub.git
cd neohub

# 2. Copy env file
cp .env.example .env
# Edit .env and add your GitHub OAuth credentials (see below)

# 3. Open in VS Code from WSL2
code .

# 4. VS Code will prompt: "Reopen in Container" — click it
# Everything installs automatically via scripts/setup.sh
```

---

### Option B — Manual (without DevContainer)

```bash
# 1. Clone and setup env
git clone https://github.com/bhumitbedse/neohub.git
cd neohub
cp .env.example .env

# 2. Start only the infrastructure (DB + Redis)
docker compose up postgres redis pgadmin redis-commander mailpit -d

# 3. Start Spring Boot (in separate terminal)
cd backend/api-gateway
mvn spring-boot:run

# 4. Start Angular (in separate terminal)
cd frontend
npm install
ng serve

# 5. Build CLI (in separate terminal)
cd cli
go run main.go version
```

---

### GitHub OAuth Setup

1. Go to [github.com/settings/developers](https://github.com/settings/developers)
2. Click **New OAuth App**
3. Fill in:
   - **Homepage URL:** `http://localhost:4200`
   - **Callback URL:** `http://localhost:4200/auth/callback`
4. Copy **Client ID** and **Client Secret** into your `.env` file

---

### GitHub Token (for scraper)

1. Go to [github.com/settings/tokens](https://github.com/settings/tokens)
2. Generate a **Classic Token** with `public_repo` scope (read-only)
3. Add it to `.env` as `GITHUB_TOKEN`

---

## Local URLs

| Service | URL |
|---|---|
| Angular Frontend | http://localhost:4200 |
| API Gateway | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui |
| pgAdmin | http://localhost:8091 |
| Redis Commander | http://localhost:8090 |
| Email (Mailpit) | http://localhost:8025 |

---

## Project Structure

```
neohub/
├── .devcontainer/          # VS Code DevContainer config
├── backend/
│   ├── api-gateway/        # Spring Boot — main entry point, JWT auth
│   ├── plugin-service/     # Spring Boot — plugin CRUD, search
│   ├── user-service/       # Spring Boot — users, GitHub OAuth
│   └── scraper-service/    # Spring Boot — GitHub scraper, scheduled jobs
├── frontend/               # Angular 17 app
├── cli/                    # Go CLI (neostore)
│   ├── cmd/                # Cobra commands (install, remove, doctor...)
│   └── internal/
│       ├── detector/       # Detects lazy.nvim / packer / vim-plug
│       ├── installer/      # Injects plugin snippets into config
│       └── api/            # NeoHub REST API client
├── docker/
│   ├── postgres/init.sql   # Full DB schema — runs on first startup
│   └── pgadmin/            # pgAdmin auto-config
├── scripts/setup.sh        # Post-create setup script
├── docker-compose.yml      # Local dev — all services
├── .env.example            # Copy to .env and fill in secrets
└── README.md
```

---

## WSL2 Performance Tips

```bash
# Add to ~/.wslconfig on Windows (C:\Users\YourName\.wslconfig)
[wsl2]
memory=8GB
processors=4
swap=2GB

# Restart WSL after:
wsl --shutdown
```

Mount your project inside WSL2 filesystem (`~/projects/neohub`), not on the Windows drive (`/mnt/c/...`). This makes Docker volumes 10x faster.

---

## Tech Stack

| Layer | Tech |
|---|---|
| Frontend | Angular 17, TypeScript, Tailwind CSS |
| Backend | Java 21, Spring Boot 3.2, Maven |
| Database | PostgreSQL 16 (full-text search via tsvector) |
| Cache | Redis 7 |
| CLI | Go 1.22, Cobra, Bubbletea |
| Auth | GitHub OAuth2 + JWT |
| Email (local) | Mailpit (SMTP mock) |
| Containers | Docker, Docker Compose |

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md). PRs welcome!

---

## License

MIT
