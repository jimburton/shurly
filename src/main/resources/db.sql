DROP DATABASE IF EXISTS shurly;
DROP USER IF EXISTS 'shurly'@'localhost';

CREATE DATABASE shurly;
CREATE USER 'shurly'@'localhost' IDENTIFIED BY 'shurly';
GRANT ALL PRIVILEGES ON * . * TO 'shurly'@'localhost';

USE shurly;

CREATE TABLE urls ( enc varchar(50) not null ,
  url varchar(2048) not null,
  constraint pk_urls primary key (enc) );



