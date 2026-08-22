CREATE TABLE users (
                       id              UUID PRIMARY KEY,
                       email           VARCHAR(255) NOT NULL UNIQUE,
                       password        VARCHAR(255) NOT NULL,
                       full_name       VARCHAR(255) NOT NULL,
                       role            VARCHAR(30)  NOT NULL,
                       enabled         BOOLEAN      NOT NULL DEFAULT TRUE,
                       created_at      TIMESTAMP    NOT NULL DEFAULT now(),
                       updated_at      TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE drivers (
                         id                UUID PRIMARY KEY,
                         full_name         VARCHAR(255) NOT NULL,
                         email             VARCHAR(255) NOT NULL UNIQUE,
                         phone             VARCHAR(30)  NOT NULL,
                         license_number    VARCHAR(50)  NOT NULL UNIQUE,
                         license_type      VARCHAR(20)  NOT NULL,
                         license_expiry    DATE         NOT NULL,
                         active            BOOLEAN      NOT NULL DEFAULT TRUE,
                         created_at        TIMESTAMP    NOT NULL DEFAULT now(),
                         updated_at        TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE vehicles (
                          id                  UUID PRIMARY KEY,
                          make                VARCHAR(100) NOT NULL,
                          model               VARCHAR(100) NOT NULL,
                          year                INTEGER      NOT NULL,
                          license_plate       VARCHAR(20)  NOT NULL UNIQUE,
                          vin                 VARCHAR(50)  UNIQUE,
                          status              VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
                          odometer_km         INTEGER      NOT NULL DEFAULT 0,
                          assigned_driver_id  UUID         REFERENCES drivers(id) ON DELETE SET NULL,
                          last_latitude       DOUBLE PRECISION,
                          last_longitude      DOUBLE PRECISION,
                          last_location_at    TIMESTAMP,
                          created_at          TIMESTAMP    NOT NULL DEFAULT now(),
                          updated_at          TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE maintenance_records (
                                     id                UUID PRIMARY KEY,
                                     vehicle_id        UUID         NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
                                     maintenance_type  VARCHAR(30)  NOT NULL,
                                     service_date      DATE         NOT NULL,
                                     next_due_date     DATE,
                                     odometer_km       INTEGER,
                                     cost              NUMERIC(10,2),
                                     notes             TEXT,
                                     created_at        TIMESTAMP    NOT NULL DEFAULT now(),
                                     updated_at        TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE TABLE location_updates (
                                  id            UUID PRIMARY KEY,
                                  vehicle_id    UUID             NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
                                  latitude      DOUBLE PRECISION NOT NULL,
                                  longitude     DOUBLE PRECISION NOT NULL,
                                  recorded_at   TIMESTAMP        NOT NULL
);