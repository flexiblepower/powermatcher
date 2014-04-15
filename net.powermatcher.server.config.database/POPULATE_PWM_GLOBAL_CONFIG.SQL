-- -------------------------------------------
-- PWM Global Configuration Group 
-- -------------------------------------------
-- The PWM Global configuration extends the Root configuration providing
-- configuration templates for PowerMatcher core components.  

-- Configuration Group PWM Global
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('PWM_Global', 'pwm_global', 0, 2, 'Root', 'root');


-- ---------------------
-- PWM Global Templates
-- ---------------------

-- PWM Global defines the following templates
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
-- The following templates are inherited from the Root configuration:
-- - org.apache.ace.scheduler
-- - net.powermatcher.core.config.management.agent.ConfigManager
-- - net.powermatcher.core.scheduler.SchedulerAdapterFactory
-- - net.powermatcher.core.scheduler.TimeAdapterFactory
-- - net.powermatcher.core.messaging.mqttv3.Mqttv3ConnectionFactory
-- - net.powermatcher.expeditor.messaging.mqttv5.Mqttv5ConnectionFactory
-- - net.powermatcher.expeditor.broker.manager.BrokerManager
-- - net.powermatcher.expeditor.broker.manager.Pipe

-- Commmon properties for PowerMatcher Global (common properties should be commented out from templates to allow override on lower level)
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'pwm_global', 'agent.adapter.factory', 'agentProtocolAdapterFactory', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'pwm_global', 'matcher.adapter.factory', 'matcherProtocolAdapterFactory', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'pwm_global', 'logging.adapter.factory', 'loggingAdapterFactory', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'pwm_global', 'telemetry.adapter.factory', 'telemetryAdapterFactory', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'pwm_global', 'log.listener.id', 'csvlogging', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'pwm_global', 'telemetry.listener.id', 'telemetrycsvlogging', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'pwm_global', 'update.interval', '30', 2);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'pwm_global', 'bid.expiration.time', '300', 2);

-- Market Basis Adapter Factory template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('PWM_Global', 'market_basis_adapter_template', 'net.powermatcher.core.agent.marketbasis.adapter.MarketBasisAdapterFactory', 1, 0, 'PWM_Global', 'pwm_global');

-- Market Basis Adapter Factory template properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'market_basis_adapter_template', 'id', 'marketBasisAdapterFactory', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'market_basis_adapter_template', 'commodity', 'electricity', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'market_basis_adapter_template', 'currency', 'EUR', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'market_basis_adapter_template', 'minimum.price', '-127', 4);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'market_basis_adapter_template', 'maximum.price', '127', 4);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'market_basis_adapter_template', 'price.steps', '255', 2);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'market_basis_adapter_template', 'significance', '0', 2);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'market_basis_adapter_template', 'market.ref', '0', 2);


-- Direct Protocol Adapter Factory template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('PWM_Global', 'direct_protocol_adapter_template', 'net.powermatcher.core.direct.protocol.adapter.component.DirectProtocolAdapterFactory', 1, 0, 'PWM_Global', 'pwm_global');

-- Direct Protocol Adapter Factory template properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'direct_protocol_adapter_template', 'id', 'directProtocolAdapterFactory', 0);


-- Direct Logging Adapter Factory template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('PWM_Global', 'direct_logging_adapter_template', 'net.powermatcher.core.direct.protocol.adapter.component.DirectLoggingAdapterFactory', 1, 0, 'PWM_Global', 'pwm_global');

-- Direct Logging Adapter Factory template properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'direct_logging_adapter_template', 'id', 'directLoggingAdapterFactory', 0);


-- Direct Telemetry Adapter Factory template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('PWM_Global', 'direct_telemetry_adapter_template', 'net.powermatcher.core.direct.protocol.adapter.component.DirectTelemetryAdapterFactory', 1, 0, 'PWM_Global', 'pwm_global');

-- Direct Telemetry Adapter Factory template properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'direct_telemetry_adapter_template', 'id', 'directTelemetryAdapterFactory', 0);


-- Agent Protocol Adapter Factory template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('PWM_Global', 'agent_adapter_template', 'net.powermatcher.core.messaging.protocol.adapter.AgentProtocolAdapterFactory', 1, 0, 'PWM_Global', 'pwm_global');

-- Agent Protocol Adapter Factory template properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'agent_adapter_template', 'id', 'agentProtocolAdapterFactory', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'agent_adapter_template', 'messaging.adapter.factory', 'mqttv3ConnectionFactory', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'agent_adapter_template', 'messaging.protocol', 'INTERNAL_v1', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'agent_adapter_template', 'bid.topic.suffix', 'UpdateBid', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'agent_adapter_template', 'price.info.topic.suffix', 'UpdatePriceInfo', 0);


-- Matcher Protocol Adapter Factory template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('PWM_Global', 'matcher_adapter_template', 'net.powermatcher.core.messaging.protocol.adapter.MatcherProtocolAdapterFactory', 1, 0, 'PWM_Global', 'pwm_global');

-- Matcher Protocol Adapter Factory template properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'matcher_adapter_template', 'id', 'matcherProtocolAdapterFactory', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'matcher_adapter_template', 'messaging.adapter.factory', 'mqttv3ConnectionFactory', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'matcher_adapter_template', 'messaging.protocol', 'INTERNAL_v1', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'matcher_adapter_template', 'bid.topic.suffix', 'UpdateBid', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'matcher_adapter_template', 'price.info.topic.suffix', 'UpdatePriceInfo', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'matcher_adapter_template', 'log.topic.suffix', 'Log', 0);


-- Logging Adapter Factory template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('PWM_Global', 'logging_adapter_template', 'net.powermatcher.core.messaging.protocol.adapter.LoggingAdapterFactory', 1, 0, 'PWM_Global', 'pwm_global');

-- Logging Adapter Factory template properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'logging_adapter_template', 'id', 'loggingAdapterFactory', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'logging_adapter_template', 'messaging.adapter.factory', 'mqttv3ConnectionFactory', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'logging_adapter_template', 'connector.id', 'loggingAdapter', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'logging_adapter_template', 'bid.topic.suffix', 'UpdateBid', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'logging_adapter_template', 'price.info.topic.suffix', 'UpdatePriceInfo', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'logging_adapter_template', 'log.topic.suffix', 'Log', 0);


-- Log Listener Adapter Factory template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('PWM_Global', 'log_listener_adapter_template', 'net.powermatcher.core.messaging.protocol.adapter.LogListenerAdapterFactory', 1, 0, 'PWM_Global', 'pwm_global');

-- Log Listener Adapter Factory template
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'log_listener_adapter_template', 'id', 'logListenerAdapterFactory', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'log_listener_adapter_template', 'messaging.adapter.factory', 'mqttv3ConnectionFactory', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'log_listener_adapter_template', 'bid.topic.suffix', 'UpdateBid', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'log_listener_adapter_template', 'price.info.topic.suffix', 'UpdatePriceInfo', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'log_listener_adapter_template', 'log.topic.suffix', 'Log', 0);


-- Telemetry Adapter Factory template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('PWM_Global', 'telemetry_adapter_template', 'net.powermatcher.telemetry.messaging.protocol.adapter.TelemetryAdapterFactory', 1, 0, 'PWM_Global', 'pwm_global');

-- Telemetry Adapter Factory template properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'telemetry_adapter_template', 'id', 'telemetryAdapterFactory', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'telemetry_adapter_template', 'messaging.adapter.factory', 'mqttv3ConnectionFactory', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'telemetry_adapter_template', 'connector.id', 'telemetryAdapter', 0);

-- Telemetry Listener Adapter Factory template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('PWM_Global', 'telemetry_listener_adapter_template', 'net.powermatcher.telemetry.messaging.protocol.adapter.TelemetryListenerAdapterFactory', 1, 0, 'PWM_Global', 'pwm_global');

-- Telemetry Listener Adapter Factory template properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'telemetry_listener_adapter_template', 'id', 'telemetryListenerAdapterFactory', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'telemetry_listener_adapter_template', 'messaging.adapter.factory', 'mqttv3ConnectionFactory', 0);


-- CSV Logging Agent template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('PWM_Global', 'csvlogging_agent_template', 'net.powermatcher.core.agent.logging.CSVLoggingAgent', 1, 0, 'PWM_Global', 'pwm_global');

-- CSV Logging Agent template properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'csvlogging_agent_template', 'id', 'csvlogging', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'csvlogging_agent_template', 'log.listener.adapter.factory', 'logListenerAdapterFactory', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'csvlogging_agent_template', 'enabled', 'true', 1);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'csvlogging_agent_template', 'update.interval', '30', 2);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'csvlogging_agent_template', 'powermatcher.bid.logging.pattern', '''pwm_bid_log_''yyyyMMdd''.csv''', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'csvlogging_agent_template', 'powermatcher.price.logging.pattern', '''pwm_price_log_''yyyyMMdd''.csv''', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'csvlogging_agent_template', 'list.separator', ';', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'csvlogging_agent_template', 'date.format', 'yyyy-MM-dd HH:mm:ss', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'csvlogging_agent_template', 'bid.expiration.time', '300', 2);

-- Telemetry CSV Logging Agent template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('PWM_Global', 'telemetrycsvlogging_agent_template', 'net.powermatcher.agent.telemetry.logging.TelemetryCSVLoggingAgent', 1, 0, 'PWM_Global', 'pwm_global');

-- Telemetry CSV Logging Agent template properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'telemetrycsvlogging_agent_template', 'id', 'telemetrycsvlogging', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'telemetrycsvlogging_agent_template', 'telemetry.listener.adapter.factory', 'telemetryListenerAdapterFactory', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'telemetrycsvlogging_agent_template', 'enabled', 'true', 1);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'telemetrycsvlogging_agent_template', 'update.interval', '30', 2);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'telemetrycsvlogging_agent_template', 'measurement.logging.pattern', '''measurement_log_''yyyyMMdd''.csv''', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'telemetrycsvlogging_agent_template', 'status.logging.pattern', '''status_log_''yyyyMMdd''.csv''', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'telemetrycsvlogging_agent_template', 'list.separator', ';', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'telemetrycsvlogging_agent_template', 'date.format', 'yyyy-MM-dd HH:mm:ss', 0);


-- Auctioneer template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('PWM_Global', 'auctioneer_template', 'net.powermatcher.core.agent.auctioneer.Auctioneer', 1, 0, 'PWM_Global', 'pwm_global');

-- Auctioneer template properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'auctioneer_template', 'id', 'auctioneer', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'auctioneer_template', 'agent.adapter.factory', 'marketBasisAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'auctioneer_template', 'matcher.adapter.factory', 'matcherProtocolAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'auctioneer_template', 'logging.adapter.factory', 'loggingAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'auctioneer_template', 'log.listener.id', 'csvlogging', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'auctioneer_template', 'enabled', 'true', 1);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'auctioneer_template', 'update.interval', '30', 2);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'auctioneer_template', 'bid.expiration.time', '300', 2);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'auctioneer_template', 'matcher.agent.bid.log.level', 'FULL_LOGGING', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'auctioneer_template', 'matcher.aggregated.bid.log.level', 'FULL_LOGGING', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'auctioneer_template', 'matcher.price.log.level', 'FULL_LOGGING', 0);

-- Objective Agent template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('PWM_Global', 'objective_agent_template', 'net.powermatcher.core.agent.objective.ObjectiveAgent', 1, 0, 'PWM_Global', 'pwm_global');

-- Objective Agent template properties
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'objective_agent_template', 'agent.adapter.factory', 'agentProtocolAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'objective_agent_template', 'matcher.adapter.factory', 'matcherProtocolAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'objective_agent_template', 'logging.adapter.factory', 'loggingAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'objective_agent_template', 'objective.adapter.factory', 'objectiveAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'objective_agent_template', 'log.listener.id', 'csvlogging', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'objective_agent_template', 'enabled', 'true', 1);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'objective_agent_template', 'update.interval', '30', 2);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'objective_agent_template', 'bid.expiration.time', '300', 2);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'objective_agent_template', 'objective.bid', '(0,0.0)', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'objective_agent_template', 'agent.bid.log.level', 'NO_LOGGING', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'objective_agent_template', 'agent.price.log.level', 'NO_LOGGING', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'objective_agent_template', 'matcher.agent.bid.log.level', 'NO_LOGGING', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'objective_agent_template', 'matcher.aggregated.bid.log.level', 'NO_LOGGING', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'objective_agent_template', 'matcher.price.log.level', 'NO_LOGGING', 0);


-- Concentrator template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('PWM_Global', 'concentrator_template', 'net.powermatcher.core.agent.concentrator.Concentrator', 1, 0, 'PWM_Global', 'pwm_global');

-- Concentrator template properties
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'concentrator_template', 'agent.adapter.factory', 'agentProtocolAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'concentrator_template', 'matcher.adapter.factory', 'matcherProtocolAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'concentrator_template', 'logging.adapter.factory', 'loggingAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'concentrator_template', 'log.listener.id', 'csvlogging', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'concentrator_template', 'enabled', 'true', 1);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'concentrator_template', 'update.interval', '30', 2);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'concentrator_template', 'bid.expiration.time', '300', 2);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'concentrator_template', 'agent.bid.log.level', 'NO_LOGGING', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'concentrator_template', 'agent.price.log.level', 'NO_LOGGING', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'concentrator_template', 'matcher.agent.bid.log.level', 'FULL_LOGGING', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'concentrator_template', 'matcher.aggregated.bid.log.level', 'NO_LOGGING', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'concentrator_template', 'matcher.price.log.level', 'FULL_LOGGING', 0);

-- Clipping Concentrator template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('PWM_Global', 'clipping_concentrator_template', 'net.powermatcher.agent.peakshavingconcentrator.ClippingConcentrator', 1, 0, 'PWM_Global', 'pwm_global');

-- Clipping Concentrator template properties
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'clipping_concentrator_template', 'agent.adapter.factory', 'agentProtocolAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'clipping_concentrator_template', 'matcher.adapter.factory', 'matcherProtocolAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'clipping_concentrator_template', 'logging.adapter.factory', 'loggingAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'clipping_concentrator_template', 'log.listener.id', 'csvlogging', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'clipping_concentrator_template', 'enabled', 'true', 1);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'clipping_concentrator_template', 'update.interval', '30', 2);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'clipping_concentrator_template', 'bid.expiration.time', '300', 2);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'clipping_concentrator_template', 'peak.shaving.enabled', 'true', 1);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'clipping_concentrator_template', 'immediate.update', 'true', 1);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'clipping_concentrator_template', 'power.upper.limit', '1000', 4);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'clipping_concentrator_template', 'power.lower.limit', '0', 4);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'clipping_concentrator_template', 'agent.bid.log.level', 'NO_LOGGING', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'clipping_concentrator_template', 'agent.price.log.level', 'NO_LOGGING', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'clipping_concentrator_template', 'matcher.agent.bid.log.level', 'FULL_LOGGING', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'clipping_concentrator_template', 'matcher.aggregated.bid.log.level', 'NO_LOGGING', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'clipping_concentrator_template', 'matcher.price.log.level', 'FULL_LOGGING', 0);


-- Peak Shaving Concentrator template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('PWM_Global', 'peak_shaving_concentrator_template', 'net.powermatcher.agent.peakshavingconcentrator.PeakShavingConcentrator', 1, 0, 'PWM_Global', 'pwm_global');

-- Peak Shaving Concentrator template properties
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'peak_shaving_concentrator_template', 'agent.adapter.factory', 'agentProtocolAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'peak_shaving_concentrator_template', 'matcher.adapter.factory', 'matcherProtocolAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'peak_shaving_concentrator_template', 'logging.adapter.factory', 'loggingAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'peak_shaving_concentrator_template', 'log.listener.id', 'csvlogging', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'peak_shaving_concentrator_template', 'enabled', 'true', 1);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'peak_shaving_concentrator_template', 'immediate.update', 'true', 1);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'peak_shaving_concentrator_template', 'update.interval', '30', 2);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'peak_shaving_concentrator_template', 'bid.expiration.time', '300', 2);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'peak_shaving_concentrator_template', 'agent.bid.log.level', 'NO_LOGGING', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'peak_shaving_concentrator_template', 'agent.price.log.level', 'NO_LOGGING', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'peak_shaving_concentrator_template', 'matcher.agent.bid.log.level', 'FULL_LOGGING', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'peak_shaving_concentrator_template', 'matcher.aggregated.bid.log.level', 'NO_LOGGING', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'peak_shaving_concentrator_template', 'matcher.price.log.level', 'FULL_LOGGING', 0);


-- Test Agent template
INSERT INTO CONFMGR.CONFIGURATION (CLUSTER_ID, CONFIG_ID, PID, TEMPLATE, "TYPE", PARENT_CLUSTER_ID, PARENT_CONFIG_ID ) VALUES ('PWM_Global', 'testagent_template', 'net.powermatcher.core.agent.test.TestAgent', 1, 0, 'PWM_Global', 'pwm_global');

-- Test Agent template properties
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'testagent_template', 'agent.adapter.factory', 'directAgentProtocolAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'testagent_template', 'logging.adapter.factory', 'loggingAdapterFactory', 0);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'testagent_template', 'log.listener.id', 'csvlogging', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'testagent_template', 'enabled', 'true', 1);
-- INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'testagent_template', 'update.interval', '30', 2);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'testagent_template', 'minimum.price', '0', 2); 
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'testagent_template', 'maximum.price', '120', 2);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'testagent_template', 'minimum.power', '0', 4);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'testagent_template', 'maximum.power', '100', 4);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'testagent_template', 'steps', '12', 2);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'testagent_template', 'agent.bid.log.level', 'NO_LOGGING', 0);
INSERT INTO CONFMGR.PROPERTY (CLUSTER_ID, CONFIG_ID, NAME, VALUE, "TYPE") VALUES ('PWM_Global', 'testagent_template', 'agent.price.log.level', 'NO_LOGGING', 0);

