# Client schema

# --- !Ups

CREATE TABLE `client` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `balance` decimal(10,2),
  `coffee_type` varchar(50),
  `milk_type` varchar(50),
  primary key (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

# --- !Downs

DROP TABLE `CLIENT`;
