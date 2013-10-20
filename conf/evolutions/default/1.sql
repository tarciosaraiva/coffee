-- Table: client

# --- !Ups

CREATE TABLE client
(
  id         SERIAL                 NOT NULL,
  first_name CHARACTER VARYING(255) NOT NULL,
  last_name  CHARACTER VARYING(255),
  balance    NUMERIC(10, 2),
  email      CHARACTER VARYING(255),
  twitter    CHARACTER VARYING(255),
  dob        DATE,
  CONSTRAINT pk_client PRIMARY KEY (id)
) WITH (OIDS = FALSE);

CREATE INDEX idx_client_name ON client (first_name ASC NULLS LAST);

CREATE TABLE transaction
(
  id               SERIAL                      NOT NULL,
  transaction_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  credit           BOOLEAN                     NOT NULL,
  amount           NUMERIC(10, 2)              NOT NULL,
  notes            CHARACTER VARYING(255),
  client_id        BIGINT,
  CONSTRAINT pk_transaction PRIMARY KEY (id),
  CONSTRAINT fk_transaction_client FOREIGN KEY (client_id)
  REFERENCES client (id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE CASCADE
) WITH (OIDS = FALSE);

CREATE INDEX idx_transaction_date ON transaction (transaction_date ASC NULLS LAST);

CREATE TABLE setting (
  id    SERIAL                 NOT NULL,
  key   CHARACTER VARYING(255) NOT NULL,
  value TEXT                   NOT NULL,
  CONSTRAINT pk_setting PRIMARY KEY (id)
);

CREATE INDEX idx_setting_key ON setting (key ASC NULLS LAST);

# --- !Downs

DROP TABLE IF EXISTS transaction;
DROP TABLE IF EXISTS client;
DROP TABLE IF EXISTS setting;
