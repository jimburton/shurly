DROP DATABASE IF EXISTS shurly;
DROP USER IF EXISTS 'shurly'@'localhost';

CREATE DATABASE shurly;
GRANT ALL PRIVILEGES ON shurly.* TO 'shurly'@'localhost' IDENTIFIED BY 'shurly';

USE shurly;

CREATE TABLE urls ( enc varchar(50) not null ,
  url varchar(2048) not null,
  constraint pk_urls primary key (enc) );



