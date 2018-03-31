DROP DATABASE IF EXISTS shurly;
CREATE DATABASE shurly;
GRANT ALL PRIVILEGES ON shurly.* TO 'shurly'@'localhost' IDENTIFIED BY 'shurly';

USE shurly;

DROP TABLE IF EXISTS urls;

CREATE TABLE urls ( id smallint unsigned not null auto_increment,
  url varchar(255) not null,
  enc varchar(50),
  constraint pk_urls primary key (id) );
