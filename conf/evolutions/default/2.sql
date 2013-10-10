# Transaction schema

# --- !Ups

CREATE TABLE `transaction` (
  `transaction_date` datetime not null,
  `credit` tinyint(1) not null,
  `amount` decimal(10,2) not null,
  `coffee_type` varchar(50),
  `milk_type` varchar(50),
  `client_id` int(11)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

alter table `transaction` add constraint `fk_transaction_client` foreign key (`client_id`) references `client` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;
create index `idx_transaction_client` on `transaction` (`client_id`);
create index `idx_transaction_date` on `transaction` (`transaction_date`);

# --- !Downs

DROP TABLE `transaction`;
