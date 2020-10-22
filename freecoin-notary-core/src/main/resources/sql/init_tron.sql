/*!40101 SET NAMES utf8 */;

/*!40101 SET SQL_MODE=''*/;

/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
CREATE DATABASE /*!32312 IF NOT EXISTS*/`freecoin` /*!40100 DEFAULT CHARACTER SET utf8 */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `freecoin`;

/*Table structure for table `tron_deposit` */

DROP TABLE IF EXISTS `tron_deposit`;

CREATE TABLE `tron_deposit` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `block_num` bigint unsigned NOT NULL COMMENT 'block num',
  `sender_on_side_chain` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'sender on side chain',
  `amount` bigint unsigned NOT NULL COMMENT 'amount',
  `tx_on_side_chain` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'tx on side chain',
  `tx_on_main_chain` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL COMMENT 'tx on main chain',
  `tx_index_on_side_chain` int unsigned NOT NULL COMMENT 'tx index on side chain',
  `status` tinyint unsigned NOT NULL COMMENT 'status',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_block_num_tx` (`block_num`,`tx_index_on_side_chain`),
  UNIQUE KEY `uk_tx_id` (`tx_on_side_chain`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `tron_deposit_meta` */

DROP TABLE IF EXISTS `tron_deposit_meta`;

CREATE TABLE `tron_deposit_meta` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `block_num` bigint unsigned NOT NULL COMMENT 'block num',
  `tx_index_on_side_chain` int unsigned NOT NULL COMMENT 'tx index on side chain',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_block_num_tx` (`block_num`,`tx_index_on_side_chain`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

/*Table structure for table `tron_deposit_address` */

DROP TABLE IF EXISTS `tron_deposit_address`;

CREATE TABLE `tron_deposit_address` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT 'id',
  `address` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL COMMENT 'address',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_address` (`address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP table IF EXISTS t_eth_withdraw;

create table if not exists t_eth_withdraw
(
    id bigint auto_increment
        primary key,
    recipient varchar(128) not null,
    tx_on_side_chain varchar(128),
    burn_proposal_id bigint not null,
    amount_on_side_chain bigint not null,
    amount bigint not null,
    approve bigint not null,
    status varchar(2) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP table IF EXISTS t_eth_notary;
create table t_eth_notary
(
    id bigint auto_increment
        primary key,
    credentials varchar(256) null,
    type int default 0 null
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into t_eth_notary(credentials, type) values("F3csxpm22CpqWw7EFKiltUpExraxfiyw8CkGqyEzvJdY6GRUKxgJHEa4ufU0bfZ7jx4fNtBygYrQe39mBIZJ3aM8eJQRvaYRnGyfYU3isRQy8XyKJSOPv6Kk6ozzWw8ZuKH33bu/iLDY0Bm5f/5B26msGSpDnNbfcnfjDlzqC+0=", "0");
insert into t_eth_notary(credentials, type) values("Bf57nCVcRUVVWf1VloenvvkHSoC8JTF+UOhfIEOC8Gy+jvin/Seof2lhA/vPqYQyiblPDK5EVaQiqH7ILktpUDmFcukkqKiiWctqDyS9UKIsvCYUZc8FlkFDiwR687gzpbVWXU2LKDuBtCUBv7Zf8GVaGuBEyVTkJbVvmn9fDP8=", "0");
insert into t_eth_notary(credentials, type) values("Bk1bhTWyRZey2nvWGOCmC39DE05PZZolRE+2rf1yZkJwq7pseg5kD6rRTL8gN8hDd2FmVjZiusV/PCvQfwsSGz3LFz8Zz/dyXShZquN+vHyt+bW8WBjJBbBwqEskQQ6Swjl41yEsg/epYomu5qRdNtNYdC5DKbD08iApBJZlbOc=", "0");
insert into t_eth_notary(credentials, type) values("F3csxpm22CpqWw7EFKiltUpExraxfiyw8CkGqyEzvJdY6GRUKxgJHEa4ufU0bfZ7jx4fNtBygYrQe39mBIZJ3aM8eJQRvaYRnGyfYU3isRQy8XyKJSOPv6Kk6ozzWw8ZuKH33bu/iLDY0Bm5f/5B26msGSpDnNbfcnfjDlzqC+0=", "1");


INSERT INTO tron_deposit_meta(block_num, tx_index_on_side_chain) VALUES(22953366, 0);
INSERT INTO tron_deposit_address(`address`) VALUES("TBt9iwwKiaM5Tuf24scDaaoQAHytPYQFq2");

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;