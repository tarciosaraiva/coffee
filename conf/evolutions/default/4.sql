# --- !Ups

ALTER TABLE transaction DROP CONSTRAINT fk_transaction_client;
ALTER TABLE transaction ADD CONSTRAINT fk_transaction_client FOREIGN KEY (client_id) REFERENCES client(id) ON DELETE CASCADE;

# --- !Downs

ALTER TABLE client DROP CONSTRAINT fk_transaction_client;
