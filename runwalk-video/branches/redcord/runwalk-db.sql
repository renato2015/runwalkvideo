
SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `runwalk-db`
--

-- --------------------------------------------------------

--
-- Table structure for table `analysis`
--

DROP TABLE IF EXISTS `analysis`;
CREATE TABLE IF NOT EXISTS `analysis` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `clientid` int(10) NOT NULL,
  `articleid` bigint(20) DEFAULT NULL,
  `date` timestamp NULL DEFAULT NULL,
  `movieid` bigint(20) DEFAULT NULL,
  `statuscode` int(4) DEFAULT NULL,
  `comments` text,
  PRIMARY KEY (`id`),
  KEY `client_fk` (`clientid`),
  KEY `article_fk` (`articleid`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=19035 ;

-- --------------------------------------------------------

--
-- Table structure for table `articles`
--

DROP TABLE IF EXISTS `articles`;
CREATE TABLE IF NOT EXISTS `articles` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `code` varchar(255) CHARACTER SET latin1 DEFAULT NULL,
  `name` varchar(255) CHARACTER SET latin1 DEFAULT NULL,
  `description` text CHARACTER SET latin1,
  `ext_url` varchar(255) CHARACTER SET latin1 DEFAULT NULL,
  `img` varchar(255) CHARACTER SET latin1 DEFAULT NULL,
  `subcategory` bigint(20) NOT NULL,
  `newsid` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM  DEFAULT CHARSET=utf8 AUTO_INCREMENT=188 ;

-- --------------------------------------------------------

--
-- Table structure for table `keyframes`
--

DROP TABLE IF EXISTS `keyframes`;
CREATE TABLE IF NOT EXISTS `keyframes` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `movie_id` bigint(20) NOT NULL,
  `position` int(16) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `movie_fk` (`movie_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=5334 ;

-- --------------------------------------------------------

--
-- Table structure for table `movies`
--

DROP TABLE IF EXISTS `movies`;
CREATE TABLE IF NOT EXISTS `movies` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `analysisid` bigint(20) DEFAULT NULL,
  `oldfilename` varchar(255) DEFAULT NULL,
  `newfilename` varchar(255) DEFAULT NULL,
  `lastmodified` bigint(20) DEFAULT NULL,
  `duration` int(20) DEFAULT NULL,
  `statuscode` bigint(20) DEFAULT '4',
  PRIMARY KEY (`id`),
  KEY `analysis_fk` (`analysisid`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=18015 ;

-- --------------------------------------------------------

--
-- Table structure for table `phppos_cities`
--

DROP TABLE IF EXISTS `phppos_cities`;
CREATE TABLE IF NOT EXISTS `phppos_cities` (
  `id` bigint(15) NOT NULL AUTO_INCREMENT,
  `code` varchar(30) CHARACTER SET latin1 COLLATE latin1_general_ci NOT NULL DEFAULT '',
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL,
  `state_id` bigint(15) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `region` (`state_id`),
  KEY `code` (`code`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=14297 ;

-- --------------------------------------------------------

--
-- Table structure for table `phppos_customers`
--

DROP TABLE IF EXISTS `phppos_customers`;
CREATE TABLE IF NOT EXISTS `phppos_customers` (
  `person_id` int(10) NOT NULL,
  `account_number` varchar(255) DEFAULT NULL,
  `taxable` int(1) NOT NULL DEFAULT '1',
  `deleted` int(1) NOT NULL DEFAULT '0',
  KEY `person_id` (`person_id`),
  KEY `account_number` (`account_number`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

-- --------------------------------------------------------

--
-- Table structure for table `phppos_people`
--

DROP TABLE IF EXISTS `phppos_people`;
CREATE TABLE IF NOT EXISTS `phppos_people` (
  `first_name` varchar(255) DEFAULT NULL,
  `last_name` varchar(255) DEFAULT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `address_1` varchar(255) DEFAULT NULL,
  `address_2` varchar(255) DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `state` varchar(255) DEFAULT NULL,
  `zip` varchar(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `comments` text,
  `person_id` int(10) NOT NULL AUTO_INCREMENT,
  `city_id` bigint(15) DEFAULT NULL,
  `in_mailing_list` tinyint(4) NOT NULL DEFAULT '1',
  `gender` tinyint(1) DEFAULT NULL,
  `birthdate` date DEFAULT NULL,
  `version` int(11) NOT NULL DEFAULT '0',
  `type` int(3) NOT NULL DEFAULT '0',
  PRIMARY KEY (`person_id`),
  KEY `type` (`type`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=8188 ;

-- --------------------------------------------------------

--
-- Table structure for table `redcord_exercises`
--

DROP TABLE IF EXISTS `redcord_exercises`;
CREATE TABLE IF NOT EXISTS `redcord_exercises` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `redcord_session_id` int(10) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `start_date` timestamp NULL DEFAULT NULL,
  `exercise_type` varchar(16) DEFAULT NULL,
  `exercise_direction` varchar(16) DEFAULT NULL,
  `comments` text,
  PRIMARY KEY (`id`),
  KEY `redcord_session_id` (`redcord_session_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=548 ;

-- --------------------------------------------------------

--
-- Table structure for table `redcord_sessions`
--

DROP TABLE IF EXISTS `redcord_sessions`;
CREATE TABLE IF NOT EXISTS `redcord_sessions` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `person_id` int(10) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `start_date` timestamp NULL DEFAULT NULL,
  `end_date` timestamp NULL DEFAULT NULL,
  `comments` text,
  `cal_id` varchar(255) DEFAULT NULL,
  `last_modified` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `person_id` (`person_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8 AUTO_INCREMENT=232 ;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `phppos_customers`
--
ALTER TABLE `phppos_customers`
  ADD CONSTRAINT `phppos_customers_ibfk_1` FOREIGN KEY (`person_id`) REFERENCES `phppos_people` (`person_id`);

--
-- Constraints for table `redcord_exercises`
--
ALTER TABLE `redcord_exercises`
  ADD CONSTRAINT `redcord_exercises_ibfk_1` FOREIGN KEY (`redcord_session_id`) REFERENCES `redcord_sessions` (`id`) ON DELETE CASCADE ON UPDATE NO ACTION;

--
-- Constraints for table `redcord_sessions`
--
ALTER TABLE `redcord_sessions`
  ADD CONSTRAINT `redcord_sessions_ibfk_1` FOREIGN KEY (`person_id`) REFERENCES `phppos_people` (`person_id`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
