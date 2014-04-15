-- -------------------------------------------
-- PowerMatcher cluster configuration template
-- -------------------------------------------

-- The PowerMatcher example configuration comprises the following components:
--
-- Configuration group Example Global defining {
--   - Example Agent 1 template
--   - Example Agent 2 template
--   - Example Agent 3 template
--   - Example Adapter Factory template
--   - Scheduler Adapter Factory
--   - Time Adapter Factory
--   - MQTTv3 Connection Factory
--   - MQTTv5 Connection Factory
--   - Direct Protocol Adapter Factory
--   - Direct Logging Adapter Factory
--   - Direct Telemetry Adapter Factory
--   - Agent Protocol Adapter Factory
--   - Matcher Protocol Adapter Factory
--   - Logging Adapter Factory
--   - Log Listener Adapter Factory
--   - Telemetry Adapter Factory
--   - Telemetry Listener Adapter Factory
--   - Example Adapter Factory
--   - Market Basis Adapter Factory
--
--   Configuration group Example Common containing {
--     MicroBroker
--     CSV Logging Agent
--     Telemetry CSV Logging Agent
--     Auctioneer
--     Objective Agent
--     Concentrator
--   }
--
--   Configuration group Core Example Agents containing {
--     Example Agent 1
--     Example Agent 2
--   }
--
--   Configuration group Extension Example Agents containing {
--     Example Agent 3
--   }
--
-- }
--
-- Dependency on the following templates inherited from the PWM_Gobal configuration:
-- - net.powermatcher.core.agent.marketbasis.adapter.MarketBasisAdapterFactory
-- - net.powermatcher.core.direct.protocol.adapter.component.DirectProtocolAdapterFactory
-- - net.powermatcher.core.direct.protocol.adapter.component.DirectLoggingAdapterFactory
-- - net.powermatcher.core.direct.protocol.adapter.component.DirectTelemetryAdapterFactory
-- - net.powermatcher.core.messaging.protocol.adapter.AgentProtocolAdapterFactory
-- - net.powermatcher.core.messaging.protocol.adapter.MatcherProtocolAdapterFactory
-- - net.powermatcher.core.messaging.protocol.adapter.LoggingAdapterFactory
-- - net.powermatcher.core.messaging.protocol.adapter.LogListenerAdapterFactory
-- - net.powermatcher.telemetry.messaging.protocol.adapter.TelemetryAdapterFactory
-- - net.powermatcher.telemetry.messaging.protocol.adapter.TelemetryListenerAdapterFactory
-- - net.powermatcher.core.agent.logging.CSVLoggingAgent
-- - net.powermatcher.agent.telemetry.logging.TelemetryCSVLoggingAgent
-- - net.powermatcher.core.agent.auctioneer.Auctioneer
-- - net.powermatcher.core.agent.objective.ObjectiveAgent
-- - net.powermatcher.core.agent.concentrator.Concentrator
-- - net.powermatcher.agent.peakshavingconcentrator.ClippingConcentrator
-- - net.powermatcher.agent.peakshavingconcentrator.PeakShavingConcentrator
-- - net.powermatcher.core.agent.test.TestAgent
--
-- Dependency on the following templates inherited from the Root configuration:
-- - org.apache.ace.scheduler
-- - net.powermatcher.core.config.management.agent.ConfigManager
-- - net.powermatcher.core.scheduler.SchedulerAdapterFactory
-- - net.powermatcher.core.scheduler.TimeAdapterFactory
-- - net.powermatcher.core.messaging.mqttv3.Mqttv3ConnectionFactory
-- - net.powermatcher.expeditor.messaging.mqttv5.Mqttv5ConnectionFactory
-- - net.powermatcher.expeditor.broker.manager.BrokerManager
-- - net.powermatcher.expeditor.broker.manager.Pipe

-- ---------------------
-- Example Global group
-- ---------------------
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'example_global', 0, 2, 'PWM_Global', 'pwm_global');

-- Commmon properties for Example Global (common properties should be commented out from templates to allow override on lower level)
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'example_global', 'cluster.id', 'ExampleCluster', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'example_global', 'agent.adapter.factory', 'directAgentProtocolAdapterFactory', 0);

-- Example Agent 1 template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'example_agent_1_template', 'net.powermatcher.core.agent.example.ExampleAgent1', 1, 0, 'ExampleCluster', 'example_global');

-- Example Agent 1 template properties
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'example_agent_1_template', 'agent.adapter.factory', 'directAgentProtocolAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'example_agent_1_template', 'logging.adapter.factory', 'loggingAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'example_agent_1_template', 'log.listener.id', 'csvlogging', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'example_agent_1_template', 'bid.price', '0.50', 4); 
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'example_agent_1_template', 'bid.power', '100', 4);


-- Example Agent 2 template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'example_agent_2_template', 'net.powermatcher.core.agent.example.ExampleAgent2', 1, 0, 'ExampleCluster', 'example_global');

-- Example Agent 2 template properties
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'example_agent_2_template', 'agent.adapter.factory', 'directAgentProtocolAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'example_agent_2_template', 'logging.adapter.factory', 'loggingAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'example_agent_2_template', 'log.listener.id', 'csvlogging', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'example_agent_2_template', 'bid.price', '0.50', 4); 
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'example_agent_2_template', 'bid.power', '100', 4);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'example_agent_2_template', 'example.setting', 'Example!', 0);


-- Example Agent 3 template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'example_agent_3_template', 'net.powermatcher.agent.example.ExampleAgent3', 1, 0, 'ExampleCluster', 'example_global');

-- Example Agent 3 template properties
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'example_agent_3_template', 'agent.adapter.factory', 'directAgentProtocolAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'example_agent_3_template', 'logging.adapter.factory', 'loggingAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'example_agent_3_template', 'telemetry.adapter.factory', 'telemetryAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'example_agent_3_template', 'log.listener.id', 'csvlogging', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'example_agent_3_template', 'telemetry.listener.id', 'telemetrycsvlogging', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'example_agent_3_template', 'bid.price', '0.50', 4); 
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'example_agent_3_template', 'bid.power', '100', 4);


-- Example Adapter Factory template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'example_adapter_template', 'net.powermatcher.messaging.adapter.example.ExampleAdapterFactory', 1, 0, 'ExampleCluster', 'example_global');

-- Example Adapter Factory template properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'example_adapter_template', 'id', 'exampleAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'example_adapter_template', 'messaging.adapter.factory', 'mqttv3ConnectionFactory', 0);


-- Scheduler Adapter Factory
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'schedulerAdapterFactory', 'net.powermatcher.core.scheduler.SchedulerAdapterFactory', 0, 0, 'ExampleCluster', 'example_global');

-- Time Adapter Factory
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'timeAdapterFactory', 'net.powermatcher.core.scheduler.TimeAdapterFactory', 0, 0, 'ExampleCluster', 'example_global');

-- MQTTv3 Connection Factory
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'mqttv3ConnectionFactory', 'net.powermatcher.core.messaging.mqttv3.Mqttv3ConnectionFactory', 0, 0, 'ExampleCluster', 'example_global');

-- MQTTv5 Connection Factory
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'mqttv5ConnectionFactory', 'net.powermatcher.expeditor.messaging.mqttv5.Mqttv5ConnectionFactory', 0, 0, 'ExampleCluster', 'example_global');

-- Direct Protocol Adapter Factory
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'directProtocolAdapterFactory', 'net.powermatcher.core.direct.protocol.adapter.component.DirectProtocolAdapterFactory', 0, 0, 'ExampleCluster', 'example_global');

-- Direct Logging Adapter Factory
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'directLoggingAdapterFactory', 'net.powermatcher.core.direct.protocol.adapter.component.DirectLoggingAdapterFactory', 0, 0, 'ExampleCluster', 'example_global');

-- Direct Telemetry Adapter Factory
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'directTelemetryAdapterFactory', 'net.powermatcher.core.direct.protocol.adapter.component.DirectTelemetryAdapterFactory', 0, 0, 'ExampleCluster', 'example_global');

-- Agent Protocol Adapter Factory
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'agentProtocolAdapterFactory', 'net.powermatcher.core.messaging.protocol.adapter.AgentProtocolAdapterFactory', 0, 0, 'ExampleCluster', 'example_global');

-- Matcher Protocol Adapter Factory
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'matcherProtocolAdapterFactory', 'net.powermatcher.core.messaging.protocol.adapter.MatcherProtocolAdapterFactory', 0, 0, 'ExampleCluster', 'example_global');

-- Logging Adapter Factory
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'loggingAdapterFactory', 'net.powermatcher.core.messaging.protocol.adapter.MatcherProtocolAdapterFactory', 0, 0, 'ExampleCluster', 'example_global');

-- Log Listener Adapter Factory
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'logListenerAdapterFactory', 'net.powermatcher.core.messaging.protocol.adapter.LogListenerAdapterFactory', 0, 0, 'ExampleCluster', 'example_global');

-- Telemetry Adapter Factory
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'telemetryAdapterFactory', 'net.powermatcher.telemetry.messaging.protocol.adapter.TelemetryAdapterFactory', 0, 0, 'ExampleCluster', 'example_global');

-- Telemetry Listener Adapter Factory
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'telemetryListenerAdapterFactory', 'net.powermatcher.telemetry.messaging.protocol.adapter.TelemetryListenerAdapterFactory', 0, 0, 'ExampleCluster', 'example_global');

-- Example Adapter Factory
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'exampleAdapterFactory', 'net.powermatcher.core.messaging.adapter.template.ExampleAdapterFactory', 0, 0, 'ExampleCluster', 'example_global');


-- Market Basis Adapter Factory
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'marketBasisAdapterFactory', 'net.powermatcher.core.agent.marketbasis.adapter.MarketBasisAdapterFactory', 0, 0, 'ExampleCluster', 'example_global');

-- Market Basis Factory properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'marketBasisAdapterFactory', 'minimum.price', '0', 4);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'marketBasisAdapterFactory', 'maximum.price', '0.99', 4);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'marketBasisAdapterFactory', 'price.steps', '100', 2);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'marketBasisAdapterFactory', 'significance', '2', 2);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'marketBasisAdapterFactory', 'market.ref', '0', 2);


-- Configuration group Example Common
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'example_common', 0, 2, 'ExampleCluster', 'example_global');

-- Broker Manager
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'MicroBroker', 'net.powermatcher.expeditor.broker.manager.BrokerManager', 0, 1, 'ExampleCluster', 'example_common');

-- Broker Manager properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'MicroBroker', 'broker.name', 'ExampleBroker', 0);

-- CSV Logging Agent
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'csvlogging', 'net.powermatcher.core.agent.logging.CSVLoggingAgent', 0, 0, 'ExampleCluster', 'example_common');

-- CSV Telemetry Logging Agent
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'telemetrycsvlogging', 'net.powermatcher.agent.telemetry.logging.TelemetryCSVLoggingAgent', 0, 0, 'ExampleCluster', 'example_common');

-- Auctioneer
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'auctioneer', 'net.powermatcher.core.agent.auctioneer.Auctioneer', 0, 0, 'ExampleCluster', 'example_common');

-- Auctioneer properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'auctioneer', 'id', 'auctioneer', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'auctioneer', 'matcher.id', '', 0);

-- Objective Agent
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'objectiveagent', 'net.powermatcher.core.agent.objective.ObjectiveAgent', 0, 0, 'ExampleCluster', 'example_common');

-- Objective Agent properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'objectiveagent', 'id', 'objectiveagent', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'objectiveagent', 'matcher.id', 'auctioneer', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'objectiveagent', 'matcher.listener.id', 'auctioneer', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'objectiveagent', 'objective.bid', '(0,-50.0)', 0);

-- Concentrator
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'concentrator', 'net.powermatcher.core.agent.concentrator.Concentrator', 0, 0, 'ExampleCluster', 'example_common');

-- Concentrator properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'concentrator', 'id', 'concentrator', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'concentrator', 'matcher.id', 'auctioneer', 0);

-- -------------------------
-- Core Example Agents group
-- -------------------------
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'core_example_agents', 0, 2, 'ExampleCluster', 'example_global');

-- Example Agent 1
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'exampleagent1', 'net.powermatcher.core.agent.example.ExampleAgent1', 0, 0, 'ExampleCluster', 'core_example_agents');

-- Example Agent 1 properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'exampleagent1', 'id', 'exampleagent1', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'exampleagent1', 'matcher.id', 'concentrator', 0);

-- Example Agent 2
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'exampleagent2', 'net.powermatcher.core.agent.example.ExampleAgent2', 0, 0, 'ExampleCluster', 'core_example_agents');

-- Example Agent 2 properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'exampleagent2', 'id', 'exampleagent2', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'exampleagent2', 'matcher.id', 'concentrator', 0);

-- ------------------------------
-- Extension Example Agents group
-- ------------------------------
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'extensions_example_agents', 0, 2, 'ExampleCluster', 'example_global');

-- Example Agent 3
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('ExampleCluster', 'exampleagent3', 'net.powermatcher.agent.example.ExampleAgent3', 0, 0, 'ExampleCluster', 'extensions_example_agents');

-- Example Agent 3 properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'exampleagent3', 'id', 'exampleagent3', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('ExampleCluster', 'exampleagent3', 'matcher.id', 'concentrator', 0);


COMMIT WORK;
