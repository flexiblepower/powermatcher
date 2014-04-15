-- -----------------------------------------
--    Node Configuration Definitions
-- -----------------------------------------
-- This scripts creates the Node Configurations.

-- Root Node : retrieves all configurations.
INSERT INTO CONFMGR.NODECONFIGURATION (NODEID, NAME, DESCRIPTION, UPDATED) VALUES ('root-node', 'Root Configuration', 'Root Configuration contains all other configurations.', 0);
INSERT INTO CONFMGR.NODECONFIGURATION_CONFIGURATION (NODECONFIGURATION_NODEID, CONFIGURATIONLIST_CLUSTER_ID, CONFIGURATIONLIST_CONFIG_ID) VALUES ('root-node', 'Root', 'root');

-- Example Node : retrieves example configurations from template.
INSERT INTO CONFMGR.NODECONFIGURATION (NODEID, NAME, DESCRIPTION, UPDATED) VALUES ('example-node', 'Example node', 'PowerMatcher example node', 0);
INSERT INTO CONFMGR.NODECONFIGURATION_CONFIGURATION (NODECONFIGURATION_NODEID, CONFIGURATIONLIST_CLUSTER_ID, CONFIGURATIONLIST_CONFIG_ID) VALUES ('example-node', 'ExampleCluster', 'example_common');
INSERT INTO CONFMGR.NODECONFIGURATION_CONFIGURATION (NODECONFIGURATION_NODEID, CONFIGURATIONLIST_CLUSTER_ID, CONFIGURATIONLIST_CONFIG_ID) VALUES ('example-node', 'ExampleCluster', 'core_example_agents');
INSERT INTO CONFMGR.NODECONFIGURATION_CONFIGURATION (NODECONFIGURATION_NODEID, CONFIGURATIONLIST_CLUSTER_ID, CONFIGURATIONLIST_CONFIG_ID) VALUES ('example-node', 'ExampleCluster', 'extensions_example_agents');

COMMIT WORK;