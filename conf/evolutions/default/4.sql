# Alterations

# --- !Ups

ALTER TABLE `client` ADD COLUMN `email` VARCHAR(255);
ALTER TABLE `client` ADD COLUMN `twitter` VARCHAR(255);
ALTER TABLE `client` ADD COLUMN `dob` DATE;
ALTER TABLE `transaction` ADD COLUMN `notes` VARCHAR(255);
ALTER TABLE `client` DROP COLUMN `coffee_type`;
ALTER TABLE `client` DROP COLUMN `milk_type`;
ALTER TABLE `transaction` DROP COLUMN `coffee_type`;
ALTER TABLE `transaction` DROP COLUMN `milk_type`;

# --- !Downs
