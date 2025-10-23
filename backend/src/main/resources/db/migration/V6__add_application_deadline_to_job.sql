ALTER TABLE jobs
    ADD application_deadline DATE;

ALTER TABLE jobs
    ALTER COLUMN application_deadline SET NOT NULL;