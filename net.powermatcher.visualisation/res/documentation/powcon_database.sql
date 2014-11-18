use powcon

CREATE TABLE 'agents' (
  'id' int(11) NOT NULL AUTO_INCREMENT,
  'agents' mediumtext,
  'settings' text,
  PRIMARY KEY ('id')
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
