-- ─────────────────────────────────────────────────────────────────────────────
--  V2 — Seed Categories
-- ─────────────────────────────────────────────────────────────────────────────
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
    ('Utilities',           'utilities',    'Miscellaneous useful plugins',                 'tool')
ON CONFLICT (slug) DO NOTHING;