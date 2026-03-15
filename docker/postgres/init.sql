-- ─────────────────────────────────────────────────
--  NeoHub — PostgreSQL Schema
--  Runs automatically on first docker compose up
-- ─────────────────────────────────────────────────

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";       -- fuzzy text search
CREATE EXTENSION IF NOT EXISTS "unaccent";       -- normalize accents in search

-- ── USERS ────────────────────────────────────────
CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    github_id       BIGINT UNIQUE NOT NULL,
    username        VARCHAR(100) UNIQUE NOT NULL,
    display_name    VARCHAR(200),
    email           VARCHAR(255),
    avatar_url      TEXT,
    github_url      TEXT,
    bio             TEXT,
    role            VARCHAR(20) DEFAULT 'USER' CHECK (role IN ('USER', 'MODERATOR', 'ADMIN')),
    is_active       BOOLEAN DEFAULT TRUE,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW()
);

-- ── PLUGIN CATEGORIES ────────────────────────────
CREATE TABLE categories (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) UNIQUE NOT NULL,
    slug        VARCHAR(100) UNIQUE NOT NULL,
    description TEXT,
    icon        VARCHAR(50)
);

INSERT INTO categories (name, slug, description, icon) VALUES
    ('LSP & Completion',    'lsp',          'Language server protocol and autocompletion',  'code'),
    ('UI & Themes',         'ui-themes',    'Colorschemes, statuslines, UI enhancements',   'palette'),
    ('File Navigation',     'navigation',   'File trees, fuzzy finders, buffers',           'folder'),
    ('Git Integration',     'git',          'Git signs, blame, diff, merge tools',          'git-branch'),
    ('Editing',             'editing',      'Motions, surround, text objects, comments',    'edit'),
    ('Terminal',            'terminal',     'Terminal emulators and multiplexers',          'terminal'),
    ('Debugging',           'debugging',    'DAP clients and debug utilities',              'bug'),
    ('Testing',             'testing',      'Test runners and coverage tools',              'check-circle'),
    ('Treesitter',          'treesitter',   'Syntax highlighting and code parsing',         'tree'),
    ('Snippets',            'snippets',     'Snippet engines and collections',              'scissors'),
    ('AI & Copilot',        'ai',           'AI assistants and code generation',            'zap'),
    ('Utilities',           'utilities',    'Miscellaneous useful plugins',                 'tool');

-- ── PLUGINS ──────────────────────────────────────
CREATE TABLE plugins (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    github_owner        VARCHAR(200) NOT NULL,
    github_repo         VARCHAR(200) NOT NULL,
    full_name           VARCHAR(400) UNIQUE NOT NULL,  -- owner/repo
    name                VARCHAR(200) NOT NULL,
    slug                VARCHAR(200) UNIQUE NOT NULL,
    description         TEXT,
    category_id         INT REFERENCES categories(id),
    tags                TEXT[],
    github_stars        INT DEFAULT 0,
    github_forks        INT DEFAULT 0,
    open_issues         INT DEFAULT 0,
    license             VARCHAR(100),
    homepage_url        TEXT,
    github_url          TEXT NOT NULL,
    neovim_min_version  VARCHAR(20),
    is_colorscheme      BOOLEAN DEFAULT FALSE,
    is_active           BOOLEAN DEFAULT TRUE,
    is_verified         BOOLEAN DEFAULT FALSE,
    submitted_by        UUID REFERENCES users(id),

    -- scraped content
    readme_raw          TEXT,
    install_guide       TEXT,
    config_example      TEXT,
    keymaps_section     TEXT,
    changelog           TEXT,
    last_scraped_at     TIMESTAMPTZ,

    -- full text search vector
    search_vector       TSVECTOR,

    created_at          TIMESTAMPTZ DEFAULT NOW(),
    updated_at          TIMESTAMPTZ DEFAULT NOW(),
    last_commit_at      TIMESTAMPTZ,

    UNIQUE(github_owner, github_repo)
);

-- Full text search index
CREATE INDEX idx_plugins_search ON plugins USING GIN(search_vector);
CREATE INDEX idx_plugins_tags ON plugins USING GIN(tags);
CREATE INDEX idx_plugins_category ON plugins(category_id);
CREATE INDEX idx_plugins_stars ON plugins(github_stars DESC);
CREATE INDEX idx_plugins_name_trgm ON plugins USING GIN(name gin_trgm_ops);

-- Auto-update search vector on insert/update
CREATE OR REPLACE FUNCTION update_plugin_search_vector()
RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector :=
        setweight(to_tsvector('english', coalesce(NEW.name, '')), 'A') ||
        setweight(to_tsvector('english', coalesce(NEW.description, '')), 'B') ||
        setweight(to_tsvector('english', coalesce(array_to_string(NEW.tags, ' '), '')), 'C');
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_plugin_search_vector
    BEFORE INSERT OR UPDATE ON plugins
    FOR EACH ROW EXECUTE FUNCTION update_plugin_search_vector();

-- ── PLUGIN MANAGER COMPATIBILITY ─────────────────
CREATE TABLE plugin_compatibility (
    id              SERIAL PRIMARY KEY,
    plugin_id       UUID REFERENCES plugins(id) ON DELETE CASCADE,
    manager         VARCHAR(50) CHECK (manager IN ('lazy.nvim', 'packer', 'vim-plug', 'dein', 'pathogen', 'manual')),
    install_snippet TEXT NOT NULL,
    config_snippet  TEXT,
    UNIQUE(plugin_id, manager)
);

-- ── USER CONFIGS ─────────────────────────────────
CREATE TABLE configs (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID REFERENCES users(id) ON DELETE CASCADE,
    title           VARCHAR(300) NOT NULL,
    slug            VARCHAR(300) NOT NULL,
    description     TEXT,
    content         TEXT NOT NULL,          -- actual config file content
    file_type       VARCHAR(20) DEFAULT 'lua' CHECK (file_type IN ('lua', 'vim', 'toml')),
    plugin_manager  VARCHAR(50),
    neovim_version  VARCHAR(20),
    use_case_tags   TEXT[],                 -- frontend, python, minimal, ide-like etc
    is_public       BOOLEAN DEFAULT TRUE,
    fork_of         UUID REFERENCES configs(id),
    views           INT DEFAULT 0,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW(),
    UNIQUE(user_id, slug)
);

CREATE INDEX idx_configs_user ON configs(user_id);
CREATE INDEX idx_configs_public ON configs(is_public, created_at DESC);
CREATE INDEX idx_configs_tags ON configs USING GIN(use_case_tags);

-- ── BOOKMARKS ────────────────────────────────────
CREATE TABLE bookmarks (
    id          SERIAL PRIMARY KEY,
    user_id     UUID REFERENCES users(id) ON DELETE CASCADE,
    plugin_id   UUID REFERENCES plugins(id) ON DELETE CASCADE,
    config_id   UUID REFERENCES configs(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ DEFAULT NOW(),
    CHECK (
        (plugin_id IS NOT NULL AND config_id IS NULL) OR
        (plugin_id IS NULL AND config_id IS NOT NULL)
    ),
    UNIQUE(user_id, plugin_id),
    UNIQUE(user_id, config_id)
);

-- ── VOTES (upvotes on plugins and configs) ────────
CREATE TABLE votes (
    id          SERIAL PRIMARY KEY,
    user_id     UUID REFERENCES users(id) ON DELETE CASCADE,
    plugin_id   UUID REFERENCES plugins(id) ON DELETE CASCADE,
    config_id   UUID REFERENCES configs(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ DEFAULT NOW(),
    CHECK (
        (plugin_id IS NOT NULL AND config_id IS NULL) OR
        (plugin_id IS NULL AND config_id IS NOT NULL)
    ),
    UNIQUE(user_id, plugin_id),
    UNIQUE(user_id, config_id)
);

-- ── DISCUSSION THREADS ───────────────────────────
CREATE TABLE threads (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    plugin_id   UUID REFERENCES plugins(id) ON DELETE CASCADE,
    config_id   UUID REFERENCES configs(id) ON DELETE CASCADE,
    author_id   UUID REFERENCES users(id) ON DELETE SET NULL,
    title       VARCHAR(500) NOT NULL,
    body        TEXT NOT NULL,
    is_solved   BOOLEAN DEFAULT FALSE,
    is_pinned   BOOLEAN DEFAULT FALSE,
    is_locked   BOOLEAN DEFAULT FALSE,
    views       INT DEFAULT 0,
    reply_count INT DEFAULT 0,
    upvotes     INT DEFAULT 0,
    created_at  TIMESTAMPTZ DEFAULT NOW(),
    updated_at  TIMESTAMPTZ DEFAULT NOW(),
    CHECK (
        (plugin_id IS NOT NULL AND config_id IS NULL) OR
        (plugin_id IS NULL AND config_id IS NOT NULL)
    )
);

CREATE INDEX idx_threads_plugin ON threads(plugin_id, created_at DESC);
CREATE INDEX idx_threads_config ON threads(config_id, created_at DESC);

-- ── REPLIES ──────────────────────────────────────
CREATE TABLE replies (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    thread_id       UUID REFERENCES threads(id) ON DELETE CASCADE,
    author_id       UUID REFERENCES users(id) ON DELETE SET NULL,
    parent_reply_id UUID REFERENCES replies(id) ON DELETE CASCADE,  -- nested replies
    body            TEXT NOT NULL,
    is_solution     BOOLEAN DEFAULT FALSE,
    upvotes         INT DEFAULT 0,
    created_at      TIMESTAMPTZ DEFAULT NOW(),
    updated_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_replies_thread ON replies(thread_id, created_at ASC);
CREATE INDEX idx_replies_parent ON replies(parent_reply_id);

-- ── SCRAPER JOB LOG ──────────────────────────────
CREATE TABLE scraper_jobs (
    id              SERIAL PRIMARY KEY,
    plugin_id       UUID REFERENCES plugins(id) ON DELETE CASCADE,
    status          VARCHAR(20) CHECK (status IN ('PENDING', 'RUNNING', 'SUCCESS', 'FAILED')),
    error_message   TEXT,
    started_at      TIMESTAMPTZ,
    completed_at    TIMESTAMPTZ,
    created_at      TIMESTAMPTZ DEFAULT NOW()
);

-- ── UPDATED_AT TRIGGER ────────────────────────────
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_users_updated_at       BEFORE UPDATE ON users       FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_plugins_updated_at     BEFORE UPDATE ON plugins     FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_configs_updated_at     BEFORE UPDATE ON configs     FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_threads_updated_at     BEFORE UPDATE ON threads     FOR EACH ROW EXECUTE FUNCTION update_updated_at();
CREATE TRIGGER trg_replies_updated_at     BEFORE UPDATE ON replies     FOR EACH ROW EXECUTE FUNCTION update_updated_at();

-- ── SEED: Popular plugins ─────────────────────────
INSERT INTO plugins (github_owner, github_repo, full_name, name, slug, description, category_id, tags, github_url, is_verified) VALUES
('nvim-telescope', 'telescope.nvim',  'nvim-telescope/telescope.nvim',  'telescope.nvim',  'telescope-nvim',  'Find, Filter, Preview, Pick — highly extendable fuzzy finder',              (SELECT id FROM categories WHERE slug='navigation'), ARRAY['fuzzy-finder','picker','search'],        'https://github.com/nvim-telescope/telescope.nvim',  true),
('nvim-tree',      'nvim-tree.lua',   'nvim-tree/nvim-tree.lua',        'nvim-tree',       'nvim-tree',       'A file explorer tree for neovim written in lua',                            (SELECT id FROM categories WHERE slug='navigation'), ARRAY['file-tree','explorer'],                  'https://github.com/nvim-tree/nvim-tree.lua',        true),
('neovim',         'nvim-lspconfig',  'neovim/nvim-lspconfig',          'nvim-lspconfig',  'nvim-lspconfig',  'Quickstart configs for Nvim LSP',                                           (SELECT id FROM categories WHERE slug='lsp'),        ARRAY['lsp','language-server'],                 'https://github.com/neovim/nvim-lspconfig',          true),
('hrsh7th',        'nvim-cmp',        'hrsh7th/nvim-cmp',               'nvim-cmp',        'nvim-cmp',        'A completion plugin for neovim coded in Lua',                               (SELECT id FROM categories WHERE slug='lsp'),        ARRAY['completion','autocomplete'],             'https://github.com/hrsh7th/nvim-cmp',               true),
('folke',          'lazy.nvim',       'folke/lazy.nvim',                 'lazy.nvim',       'lazy-nvim',       'A modern plugin manager for Neovim',                                        (SELECT id FROM categories WHERE slug='utilities'),  ARRAY['plugin-manager'],                        'https://github.com/folke/lazy.nvim',                true),
('nvim-lualine',   'lualine.nvim',    'nvim-lualine/lualine.nvim',      'lualine.nvim',    'lualine-nvim',    'A blazing fast and easy to configure statusline plugin for neovim',         (SELECT id FROM categories WHERE slug='ui-themes'),  ARRAY['statusline','ui'],                       'https://github.com/nvim-lualine/lualine.nvim',      true),
('lewis6991',      'gitsigns.nvim',   'lewis6991/gitsigns.nvim',        'gitsigns.nvim',   'gitsigns-nvim',   'Git integration for buffers',                                               (SELECT id FROM categories WHERE slug='git'),        ARRAY['git','signs','blame'],                   'https://github.com/lewis6991/gitsigns.nvim',        true),
('folke',          'which-key.nvim',  'folke/which-key.nvim',           'which-key.nvim',  'which-key-nvim',  'Create key bindings that stick',                                            (SELECT id FROM categories WHERE slug='utilities'),  ARRAY['keymaps','ui'],                          'https://github.com/folke/which-key.nvim',           true),
('catppuccin',     'nvim',            'catppuccin/nvim',                 'catppuccin',      'catppuccin',      'Soothing pastel theme for Neovim',                                          (SELECT id FROM categories WHERE slug='ui-themes'),  ARRAY['theme','colorscheme'],                   'https://github.com/catppuccin/nvim',                true),
('folke',          'tokyonight.nvim', 'folke/tokyonight.nvim',          'tokyonight.nvim', 'tokyonight-nvim', 'A clean, dark Neovim theme',                                                (SELECT id FROM categories WHERE slug='ui-themes'),  ARRAY['theme','colorscheme'],                   'https://github.com/folke/tokyonight.nvim',          true);
