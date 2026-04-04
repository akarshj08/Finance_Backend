-- Password for all users: password
INSERT INTO users (full_name, email, password, role, status)
VALUES (
    'System Admin',
    'admin@finance.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'ADMIN',
    'ACTIVE'
) ON CONFLICT (email) DO NOTHING;

INSERT INTO users (full_name, email, password, role, status)
VALUES (
    'Finance Analyst',
    'analyst@finance.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'ANALYST',
    'ACTIVE'
) ON CONFLICT (email) DO NOTHING;

INSERT INTO users (full_name, email, password, role, status)
VALUES (
    'Dashboard Viewer',
    'viewer@finance.com',
    '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi',
    'VIEWER',
    'ACTIVE'
) ON CONFLICT (email) DO NOTHING;

-- Seed categories
INSERT INTO categories (name, description) VALUES
    ('Salary',        'Monthly or periodic salary income'),
    ('Freelance',     'Freelance and contract work income'),
    ('Investment',    'Returns from investments and dividends'),
    ('Rent',          'Rental income from properties'),
    ('Food',          'Groceries and dining expenses'),
    ('Transport',     'Travel, fuel, and commuting costs'),
    ('Utilities',     'Electricity, water, internet bills'),
    ('Healthcare',    'Medical and healthcare expenses'),
    ('Entertainment', 'Movies, subscriptions, hobbies'),
    ('Education',     'Courses, books, training')
ON CONFLICT (name) DO NOTHING;