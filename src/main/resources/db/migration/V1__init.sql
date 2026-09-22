CREATE TABLE clients (
    id          BIGSERIAL PRIMARY KEY,
    telegram_id BIGINT NOT NULL UNIQUE,
    first_name  VARCHAR(255)
);

CREATE TABLE services (
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(255) NOT NULL UNIQUE,
    price INTEGER NOT NULL
);

CREATE TABLE appointments (
    id               BIGSERIAL PRIMARY KEY,
    client_id        BIGINT NOT NULL REFERENCES clients (id),
    service_id       BIGINT NOT NULL REFERENCES services (id),
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    CONSTRAINT uk_appointments_date_time UNIQUE (appointment_date, appointment_time)
);

CREATE INDEX idx_appointments_client_id ON appointments (client_id);
