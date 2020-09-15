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

INSERT INTO tron_deposit_meta(block_num, tx_index_on_side_chain) VALUES(22953366, 0);
INSERT INTO tron_deposit_address(`address`) VALUES("TBt9iwwKiaM5Tuf24scDaaoQAHytPYQFq2");

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;