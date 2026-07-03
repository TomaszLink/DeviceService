CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE devices (
     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
     name VARCHAR(128) NOT NULL,
     type VARCHAR(64) NOT NULL,
     unique_identifier VARCHAR(128) NOT NULL,
     version BIGINT NOT NULL DEFAULT 0,
     created_at TIMESTAMP WITH TIME ZONE NOT NULL,
     updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

     CONSTRAINT uk_devices_unique_identifier UNIQUE (unique_identifier)
);

CREATE TABLE device_locations (
    id UUID PRIMARY KEY,
    device_id UUID NOT NULL,
    latitude NUMERIC(9, 6) NOT NULL,
    longitude NUMERIC(9, 6) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_device_locations_device
      FOREIGN KEY (device_id)
          REFERENCES devices(id),

    CONSTRAINT chk_device_locations_latitude
      CHECK (latitude >= -90 AND latitude <= 90),

    CONSTRAINT chk_device_locations_longitude
      CHECK (longitude >= -180 AND longitude <= 180)
);

CREATE INDEX idx_device_locations_device_id_gps_timestamp
    ON device_locations (device_id, timestamp DESC);

CREATE TABLE device_last_locations (
    device_id UUID PRIMARY KEY,
    location_id UUID NOT NULL,
    latitude NUMERIC(9, 6) NOT NULL,
    longitude NUMERIC(9, 6) NOT NULL,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT fk_device_last_locations_device
       FOREIGN KEY (device_id)
           REFERENCES devices(id),

    CONSTRAINT fk_device_last_locations_event
       FOREIGN KEY (location_id)
           REFERENCES device_locations(id),

    CONSTRAINT chk_device_last_locations_latitude
       CHECK (latitude >= -90 AND latitude <= 90),

    CONSTRAINT chk_device_last_locations_longitude
       CHECK (longitude >= -180 AND longitude <= 180)
);
