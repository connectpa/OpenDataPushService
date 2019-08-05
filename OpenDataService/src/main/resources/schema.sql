CREATE TABLE metadata
(
  id VARCHAR(255) NOT NULL,
  name VARCHAR(255) NOT NULL UNIQUE,
  description VARCHAR(255) NULL,
  PRIMARY KEY(id)
);

Insert into metadata values ('1','Product','This is for test');

CREATE TABLE Product
(
  id int NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL UNIQUE,
  description VARCHAR(255) NULL
);