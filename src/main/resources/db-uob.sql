DROP DATABASE IF EXISTS shurly;

CREATE DATABASE shurly;

USE shurly;

CREATE TABLE urls (
       enc varchar(50) not null
     , url varchar(2048) not null
     , constraint pk_urls primary key (enc)
     );



