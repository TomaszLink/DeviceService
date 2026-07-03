CREATE TABLE devices (
     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     name VARCHAR(128) NOT NULL,
     type VARCHAR(64) NOT NULL,
     unique_identifier VARCHAR(128) NOT NULL,
     version BIGINT NOT NULL,
     created_at TIMESTAMP WITH TIME ZONE NOT NULL,
     updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

     CONSTRAINT uk_devices_unique_identifier UNIQUE (unique_identifier)
);
