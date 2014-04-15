-- -------------------------------------------
-- Root Configuration Group 
-- -------------------------------------------
-- The Root configuration provides the basis for
-- all projects running in OSGi. It provides configuration
-- templates for using an MQTT broker.
--
-- The Root configuration defines the following templates:
-- - org.apache.ace.scheduler
-- - net.powermatcher.core.config.management.agent.ConfigManager
-- - net.powermatcher.core.scheduler.SchedulerAdapterFactory
-- - net.powermatcher.core.scheduler.TimeAdapterFactory
-- - net.powermatcher.core.messaging.mqttv3.Mqttv3ConnectionFactory
-- - net.powermatcher.expeditor.messaging.mqttv5.Mqttv5ConnectionFactory
-- - net.powermatcher.expeditor.broker.manager.BrokerManager
-- - net.powermatcher.expeditor.broker.manager.Pipe

-- The Root configuration defines the following configurations:
-- - Apache ACE Update Scheduler Configuration

-- Configuration Group Root
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, TEMPLATE, "TYPE") VALUES ('Root', 'root', 0, 2);

-- Commmon properties for PowerMatcher Global (common properties should be commented out from templates to allow override on lower level)
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'root', 'time.adapter.factory', 'timeAdapterFactory', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'root', 'scheduler.adapter.factory', 'schedulerAdapterFactory', 0);

-- ----------------------------
-- Root configuration Templates
-- ----------------------------

-- Apache ACE Update Scheduler Template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('Root', 'ACE_scheduler_template', 'org.apache.ace.scheduler', 1, 1, 'Root', 'root');

-- Apache ACE Update Scheduler Template Properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'ACE_scheduler_template', 'org.apache.ace.deployment.task.DeploymentCheckTask', '60000', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'ACE_scheduler_template', 'org.apache.ace.deployment.task.DeploymentUpdateTask', '60000', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'ACE_scheduler_template', 'auditlog', '60000', 0);


-- Configuration Management Agent Template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('Root', 'config_management_agent_template', 'net.powermatcher.core.config.management.agent.ConfigManager', 1, 1, 'Root', 'root');

-- Configuration Management Agent Template Properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'config_management_agent_template', 'id', 'configagent', 0);


-- Scheduler Adapter Factory Template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('Root', 'scheduler_adapter_template', 'net.powermatcher.core.scheduler.SchedulerAdapterFactory', 1, 0, 'Root', 'root');

-- Scheduler Adapter Factory Template Properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'scheduler_adapter_template', 'id', 'schedulerAdapterFactory', 0);


-- Time Adapter Factory Template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('Root', 'time_adapter_template', 'net.powermatcher.core.scheduler.TimeAdapterFactory', 1, 0, 'Root', 'root');

-- Time Adapter Factory Template Properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'time_adapter_template', 'id', 'timeAdapterFactory', 0);


-- MQTT V3 Connection Factory Template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('Root', 'mqttv3_connection_template', 'net.powermatcher.core.messaging.mqttv3.Mqttv3ConnectionFactory', 1, 0, 'Root', 'root');

-- MQTT V3 Connection Factory Template Properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'mqttv3_connection_template', 'id', 'mqttv3ConnectionFactory', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'mqttv3_connection_template', 'broker.uri', 'tcp://localhost:1883', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'mqttv3_connection_template', 'reconnect.interval', '10', 2);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'mqttv3_connection_template', 'notification.enabled', 'false', 1);


-- MQTT V5 Connection Factory Template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('Root', 'mqttv5_connection_template', 'net.powermatcher.expeditor.messaging.mqttv5.Mqttv5ConnectionFactory', 1, 0, 'Root', 'root');

-- MQTT V5 Connection Factory Template Properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'mqttv5_connection_template', 'id', 'mqttv5ConnectionFactory', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'mqttv5_connection_template', 'broker.uri', 'tcp://localhost:1883', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'mqttv5_connection_template', 'reconnect.interval', '10', 2);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'mqttv5_connection_template', 'notification.enabled', 'false', 1);


-- Broker Manager Config Template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('Root', 'MicroBroker_template', 'net.powermatcher.expeditor.broker.manager.BrokerManager', 1, 1, 'Root', 'root');

-- Broker Manager Config Template Properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'MicroBroker_template', 'broker.name', 'dummy_broker_name', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'MicroBroker_template', 'microbroker.dir', '.', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'MicroBroker_template', 'microbroker.maxMessageSize', '100', 2);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'MicroBroker_template', 'microbroker.maxNumberOfClients', '-1', 2);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'MicroBroker_template', 'microbroker.persistence', '0', 2);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'MicroBroker_template', 'microbroker.port', '1883', 2);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'MicroBroker_template', 'microbroker.queueSize', '-1', 2);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'MicroBroker_template', 'microbroker.numberOfLogFilesToKeep', '10', 2);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'MicroBroker_template', 'microbroker.waitTimeout', '10000', 5);


-- Bridge Manager Config Template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('Root', 'MicroBrokerBridge_template', 'net.powermatcher.expeditor.broker.manager.Pipe', 1, 0, 'Root', 'root');

-- Bridge Manager Config Template Properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'MicroBrokerBridge_template', 'pipe.name', 'dummy_pipe_name', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'MicroBrokerBridge_template', 'id', 'dummy_id', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'MicroBrokerBridge_template', 'remote.host', 'remote_broker', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'MicroBrokerBridge_template', 'remote.port', '8883', 2);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'MicroBrokerBridge_template', 'remote.secure', 'true', 1);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'MicroBrokerBridge_template', 'keep.alive.secs', '60', 6);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'MicroBrokerBridge_template', 'out.topics', 'PowerMatcher/+/+/+/Log,Telemetry/#,Status/Agent/#', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'MicroBrokerBridge_template', 'in.topics', 'PowerMatcher/+/+/UpdatePriceInfo,Configuration/Update/#', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('Root', 'MicroBrokerBridge_template', 'notification.enabled', 'true', 1);

-- --------------------------------------------------------------------
-- Apache ACE Update Scheduler Configuration
-- --------------------------------------------------------------------

INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('Root', 'ACE_scheduler', 'org.apache.ace.scheduler', 0, 1, 'Root', 'root');

COMMIT WORK;