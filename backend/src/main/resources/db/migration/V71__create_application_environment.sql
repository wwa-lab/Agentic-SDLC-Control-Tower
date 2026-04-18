CREATE TABLE dp_application_environment (
    id VARCHAR(64) PRIMARY KEY,
    application_id VARCHAR(64) NOT NULL,
    name VARCHAR(64) NOT NULL,
    kind VARCHAR(16) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_dpenv UNIQUE (application_id, name),
    CONSTRAINT fk_dpenv_app FOREIGN KEY (application_id) REFERENCES dp_application(id)
);
