ALTER TABLE companies
    ADD COLUMN normalized_name VARCHAR(255);

CREATE UNIQUE INDEX IF NOT EXISTS idx_companies_normalized_name ON companies (normalized_name);