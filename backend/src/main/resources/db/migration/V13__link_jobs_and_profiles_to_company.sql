ALTER TABLE user_profiles
    ADD COLUMN  company_id BIGINT;

ALTER TABLE user_profiles
    ADD CONSTRAINT FK_USER_PROFILES_ON_COMPANY FOREIGN KEY (company_id) REFERENCES companies(id);

ALTER TABLE jobs
    ADD company_id BIGINT;

ALTER TABLE jobs
    ADD CONSTRAINT FK_JOBS_ON_COMPANY FOREIGN KEY (company_id) REFERENCES companies(id);

ALTER TABLE jobs
    DROP COLUMN company;

ALTER TABLE jobs
    ALTER COLUMN company_id SET NOT NULL;

