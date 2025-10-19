ALTER TABLE users
    DROP COLUMN username;

ALTER TABLE users
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE users
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE job_applications
    ALTER COLUMN applied_at SET NOT NULL;

ALTER TABLE job_applications
    ALTER COLUMN created_at SET NOT NULL;

ALTER TABLE job_applications
    ALTER COLUMN status TYPE VARCHAR(20) USING (status::VARCHAR(20));

ALTER TABLE job_applications
    ALTER COLUMN updated_at SET NOT NULL;

ALTER TABLE jobs
    ALTER COLUMN location TYPE VARCHAR(255) USING (location::VARCHAR(255));

ALTER TABLE jobs
    ALTER COLUMN company TYPE VARCHAR(255) USING (company::VARCHAR(255));

DROP INDEX idx_jobs_active;
