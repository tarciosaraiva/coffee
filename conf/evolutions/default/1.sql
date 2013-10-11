# Client schema

# --- !Ups

CREATE TABLE `client` (
  `id`          INT(11) UNSIGNED NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(255)     NOT NULL,
  `balance`     DECIMAL(10, 2),
  `coffee_type` VARCHAR(50),
  `milk_type`   VARCHAR(50),
  PRIMARY KEY (`id`)
);

# --- !Downs

DROP TABLE IF EXISTS `client`;
