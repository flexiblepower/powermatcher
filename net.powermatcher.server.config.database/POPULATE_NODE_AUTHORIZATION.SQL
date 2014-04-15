-- -----------------------------------------
--    Authorization table
-- -----------------------------------------
-- This scripts creates the Node retrieval authorizations.

-- user pwmadmin
INSERT INTO CONFMGR.AUTHORIZATION (USERID, NODEID) VALUES ('pwmadmin', 'root-node');
INSERT INTO CONFMGR.AUTHORIZATION (USERID, NODEID) VALUES ('pwmadmin', 'example-node');

COMMIT WORK;