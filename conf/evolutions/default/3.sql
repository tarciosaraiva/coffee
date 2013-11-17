# --- !Ups

ALTER TABLE client ADD COLUMN hidden BOOLEAN;
ALTER TABLE client ALTER COLUMN hidden SET DEFAULT FALSE;

# --- !Downs

ALTER TABLE client DROP COLUMN hidden;
