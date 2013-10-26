# --- !Ups

ALTER TABLE client ADD COLUMN notified BOOLEAN;
ALTER TABLE client ALTER COLUMN notified SET DEFAULT FALSE;

# --- !Downs

ALTER TABLE client DROP COLUMN notified;
