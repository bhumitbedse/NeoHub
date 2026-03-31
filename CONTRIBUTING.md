# 🤝 Contributing to NeoHub

Thanks for your interest in contributing to NeoHub 🚀

We welcome contributions of all kinds — features, bug fixes, docs, and ideas.

---

# 🧠 Project Overview

NeoHub is a full-stack developer tool:

- ⚡ CLI (`neostore`) for installing Neovim plugins
- 🌐 API Gateway + microservices (Spring Boot)
- 🗄 PostgreSQL database
- 🎨 Angular frontend

---

# 🚀 Getting Started

## 1. Fork & Clone

```bash
git clone https://github.com/<your-username>/NeoHub.git
cd NeoHub
```

---

## 2. Setup Environment

```bash
cp .env.example .env
```

Fill required values:

- GitHub OAuth credentials
- Database config (if needed)

---

## 3. Run with DevContainer (Recommended)

```bash
code .
```

👉 Click **"Reopen in Container"**

Everything will be set up automatically.

---

## 4. Manual Setup (Alternative)

```bash
docker compose up postgres redis -d

cd backend/api-gateway
mvn spring-boot:run

cd frontend
npm install
ng serve

cd cli
go run main.go version
```

---

# 🛠 Development Guidelines

---

## 📦 Backend (Spring Boot)

- Follow clean architecture
- Use DTOs (no entity exposure)
- Add validation (`@Valid`)
- Use Flyway for DB changes

---

## 🖥 CLI (Go)

- Keep commands simple and composable
- Follow existing structure (`cmd/`, `internal/`)
- Add helpful error messages
- Avoid breaking UX

---

## 🎨 Frontend (Angular)

- Keep components small
- Use Tailwind consistently
- Prefer reusable UI

---

# 🧪 Testing

Before submitting PR:

```bash
# Backend
mvn test

# CLI
go build ./...
```

Make sure:

- No build errors
- No breaking changes

---

# 🌿 Branching Strategy

- `main` → production-ready
- `feature/*` → new features
- `fix/*` → bug fixes

---

# 📝 Commit Style

Use clear messages:

```text
feat: add plugin search endpoint
fix: handle null category in response
docs: update README install section
```

---

# 🔄 Pull Request Process

1. Fork the repo
2. Create a branch:

```bash
git checkout -b feature/your-feature-name
```

3. Make your changes
4. Commit & push
5. Open a PR

---

## ✅ PR Checklist

- [ ] Code builds successfully
- [ ] No secrets added
- [ ] Follows project structure
- [ ] Tested locally

---

# 🚫 What NOT to commit

- `.env` files
- API keys / secrets
- Generated binaries (`dist/`)
- Logs

---

# 💡 Ideas to Contribute

- New CLI commands
- Better plugin search
- Performance improvements
- UI enhancements
- Documentation

---

# 🙌 Community

If you have questions or ideas:

- Open an issue
- Start a discussion

---

Thanks for contributing ❤️
