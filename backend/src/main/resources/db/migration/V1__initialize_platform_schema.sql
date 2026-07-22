CREATE TABLE platform_schema_metadata (
    schema_key VARCHAR(64) NOT NULL,
    schema_value VARCHAR(255) NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (schema_key)
);
