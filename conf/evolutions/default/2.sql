# Transaction schema

# --- !Ups

CREATE TABLE `transaction` (
  `transaction_date` DATETIME       NOT NULL,
  `credit`           BOOLEAN        NOT NULL,
  `amount`           DECIMAL(10, 2) NOT NULL,
  `coffee_type`      VARCHAR(50),
  `milk_type`        VARCHAR(50),
  `client_id`        INT(11)
);

ALTER TABLE `transaction` ADD CONSTRAINT `fk_transaction_client` FOREIGN KEY (`client_id`) REFERENCES `client` (`id`)
  ON DELETE CASCADE
  ON UPDATE NO ACTION;
CREATE INDEX `idx_transaction_client` ON `transaction` (`client_id`);
CREATE INDEX `idx_transaction_date` ON `transaction` (`transaction_date`);

# --- !Downs

DROP TABLE IF EXISTS `transaction`;
DROP INDEX `idx_transaction_client` ON `transaction`;
DROP INDEX `idx_transaction_date` ON `transaction`;
