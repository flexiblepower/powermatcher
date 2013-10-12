package net.powermatcher.fpai.controller;

import java.util.Collection;
import java.util.Dictionary;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import net.powermatcher.core.adapter.Adapter;
import net.powermatcher.core.agent.concentrator.Concentrator;
import net.powermatcher.core.agent.concentrator.framework.AbstractConcentrator;
import net.powermatcher.core.agent.framework.Agent;
import net.powermatcher.core.agent.framework.service.AgentService;
import net.powermatcher.core.agent.marketbasis.adapter.MarketBasisAdapter;
import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.configurable.PrefixedConfiguration;
import net.powermatcher.core.configurable.service.ConfigurationService;
import net.powermatcher.core.messaging.mqttv3.Mqttv3Connection;
import net.powermatcher.core.messaging.protocol.adapter.AgentProtocolAdapter;
import net.powermatcher.core.messaging.protocol.adapter.MatcherProtocolAdapter;
import net.powermatcher.core.messaging.protocol.adapter.config.ProtocolAdapterConfiguration;
import net.powermatcher.fpai.agent.FPAIAgent;
import net.powermatcher.fpai.agent.buffer.BufferAgent;
import net.powermatcher.fpai.agent.storage.StorageAgent;
import net.powermatcher.fpai.agent.timeshifter.TimeshifterAgent;
import net.powermatcher.fpai.agent.uncontrolled.UncontrolledAgent;
import net.powermatcher.fpai.controller.PMController.Config;

import org.flexiblepower.control.ControllerManager;
import org.flexiblepower.rai.ControllableResource;
import org.flexiblepower.rai.Controller;
import org.flexiblepower.rai.ResourceType;
import org.flexiblepower.time.TimeService;
import org.flexiblepower.ui.WidgetService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Modified;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

@Component(immediate = true, designateFactory = Config.class)
public class PMController implements ControllerManager {
    interface Config {
        @Meta.AD(deflt = "pvpanel,dishwasher,refrigerator,battery", cardinality = Integer.MAX_VALUE)
        String[] resourceIds();

        @Meta.AD(deflt = "ExampleCluster")
        String cluster_id();

        @Meta.AD(deflt = "ExampleLocation")
        String location_id();

        @Meta.AD(deflt = "INTERNAL_v1")
        ProtocolAdapterConfiguration.Protocol messaging_protocol();

        @Meta.AD(deflt = "UpdateBid")
        String bid_topic_suffix();

        @Meta.AD(deflt = "UpdatePriceInfo")
        String price_info_topic_suffix();

        @Meta.AD(deflt = "tcp://localhost:1883")
        String broker_uri();

        @Meta.AD(deflt = "1")
        int update_interval();

        @Meta.AD(deflt = "auctioneer1")
        String auctioneer_id();
    }

    private static final Logger logger = LoggerFactory.getLogger(PMController.class);

    /** the agent classes that can be used for specific types of resources */
    private final Map<ResourceType, Class<? extends FPAIAgent>> agentMap;

    /** the agents, mapped by the ControllableResource their attached to */
    private final ConcurrentMap<ControllableResource, FPAIAgent> agents;

    /** the counter that is used to generate unique agent ids */
    private final AtomicInteger agentId;

    /** The parsed configuration from the init method is stored here */
    private Config config;

    /** The parsed configuration as a set of properties, used for the configuring of components */
    private Map<String, Object> properties;

    /** The configuration for the concentrator as PrefixedConfiguration */
    private ConfigurationService concentratorConfiguration;

    /** the local concentrator, with which the agents are associated */
    private AbstractConcentrator concentrator;

    private ServiceRegistration<AgentService> serviceRegistration;

    public PMController() {
        agentMap = new EnumMap<ResourceType, Class<? extends FPAIAgent>>(ResourceType.class);
        agentMap.put(ResourceType.BUFFER, BufferAgent.class);
        agentMap.put(ResourceType.STORAGE, StorageAgent.class);
        agentMap.put(ResourceType.TIMESHIFTER, TimeshifterAgent.class);
        agentMap.put(ResourceType.UNCONTROLLED, UncontrolledAgent.class);

        agents = new ConcurrentHashMap<ControllableResource, FPAIAgent>();

        agentId = new AtomicInteger();
    }

    @Activate
    public void init(BundleContext context, Map<String, Object> properties) throws Exception {
        properties = new Hashtable<String, Object>(properties);
        properties.put("id", "concentrator-" + UUID.randomUUID().toString());

        config = Configurable.createConfigurable(Config.class, properties);
        this.properties = properties;

        concentratorConfiguration = new BaseConfiguration(properties);
        concentrator = createConcentrator(concentratorConfiguration);
        concentrator.bind(executorService);
        concentrator.bind(timeService);

        if (config.broker_uri() == null || config.broker_uri().isEmpty()) {
            Dictionary<String, Object> concentratorProperties = new Hashtable<String, Object>();
            concentratorProperties.put("auctioneer.id", "auctioneer1");
            serviceRegistration = context.registerService(AgentService.class, concentrator, concentratorProperties);
        } else {
            createConcentratorUplink(concentrator);
        }
    }

    protected AbstractConcentrator createConcentrator(ConfigurationService concentratorConfiguration) {
        return new Concentrator(concentratorConfiguration);
    }

    @Modified
    public void modified(BundleContext context, Map<String, Object> properties) throws Exception {
        Properties newProperties = new Properties();
        newProperties.putAll(properties);
        if (!newProperties.equals(this.properties)) {
            deactive();

            init(context, properties);

            for (FPAIAgent agent : agents.values()) {
                concentrator.bind(agent);
                agent.bind(concentrator);
            }
        }
    }

    @Deactivate
    public void deactive() {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        } else {
            deactivateConcentratorUplink(concentrator);
        }

        for (FPAIAgent agent : agents.values()) {
            concentrator.unbind(agent);
            agent.unbind(concentrator);
        }

        concentrator.unbind(executorService);
        concentrator.unbind(timeService);
        concentrator = null;
    }

    /** executor service for periodic tasks performed by agents */
    private ScheduledExecutorService executorService;

    @Reference
    public void setExecutorService(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    private net.powermatcher.core.scheduler.service.TimeService timeService;

    @Reference
    public void setTimeService(final TimeService timeService) {
        this.timeService = new net.powermatcher.core.scheduler.service.TimeService() {
            @Override
            public int getRate() {
                throw new UnsupportedOperationException();
            }

            @Override
            public long currentTimeMillis() {
                return timeService.getCurrentTimeMillis();
            }
        };
    }

    private PMWidget widget;

    @Reference(optional = true)
    public void setWidgetService(WidgetService widgetService) {
        if (widget == null) {
            widget = createWidget();
            widgetService.registerWidget(widget);

            for (Agent agent : agents.values()) {
                agent.bind(widget);
            }
        }
    }

    protected PMWidget createWidget() {
        return new PMWidgetImpl(this);
    }

    public void unsetWidgetService(WidgetService widgetService) {
        if (widget != null) {
            if (widgetService.unregisterWidget(widget)) {
                widget = null;
            }

            for (Agent agent : agents.values()) {
                agent.unbind(widget);
            }
        }
    }

    // These represent the default uplink for the concentrator
    private MarketBasisAdapter marketBasisAdapter;
    private AgentProtocolAdapter agentProtocolAdapter;
    private MatcherProtocolAdapter matcherProtocolAdapter;
    private Mqttv3Connection mqttv3Connection;

    protected void createConcentratorUplink(AbstractConcentrator concentrator) throws Exception {
        marketBasisAdapter = new MarketBasisAdapter(concentratorConfiguration);
        marketBasisAdapter.setAgentConnector(concentrator);

        agentProtocolAdapter = new AgentProtocolAdapter(concentratorConfiguration);
        agentProtocolAdapter.setAgentConnector(concentrator);
        agentProtocolAdapter.setParentMatcherId(config.auctioneer_id());

        matcherProtocolAdapter = new MatcherProtocolAdapter(concentratorConfiguration);
        matcherProtocolAdapter.setMatcherConnector(concentrator);

        mqttv3Connection = new Mqttv3Connection();
        mqttv3Connection.setConfiguration(concentratorConfiguration);
        mqttv3Connection.addConnector(agentProtocolAdapter);
        mqttv3Connection.addConnector(matcherProtocolAdapter);

        for (Adapter a : new Adapter[] { marketBasisAdapter,
                                        matcherProtocolAdapter,
                                        agentProtocolAdapter,
                                        mqttv3Connection }) {
            a.bind(executorService);
            a.bind(timeService);
            a.bind();
        }
    }

    protected void deactivateConcentratorUplink(AbstractConcentrator concentrator) {
        for (Adapter a : new Adapter[] { mqttv3Connection,
                                        matcherProtocolAdapter,
                                        agentProtocolAdapter,
                                        marketBasisAdapter }) {
            a.unbind();
            a.unbind(timeService);
            a.unbind(executorService);
        }
    }

    public Collection<? extends Agent> getAgentList() {
        return agents.values();
    }

    /**
     * creates a PowerMatcher agent which is capable of controlling a resource of the type of the controllable resource
     * (e.g. buffer, time shifter, ...)
     * 
     * @see Controller#registerResource(ControllableResource)
     */
    @Override
    public synchronized void registerResource(ControllableResource controllableResource) {
        logger.debug("Adding ControllableResource of type " + controllableResource.getResourceType());

        Class<? extends FPAIAgent> agentClass = agentMap.get(controllableResource.getResourceType());
        if (agentClass == null) {
            logger.warn("No support for ControllableResource of type: ", controllableResource.getResourceType());
            return;
        }

        String agentId = controllableResource.getResourceType().toString().toLowerCase() + this.agentId.incrementAndGet();

        // create the agent, and bind the agent to the ControllableResource
        FPAIAgent agent = createAgent(agentId, agentClass);
        agent.bind(controllableResource); // also binds ControllableResource to agent

        // bind the agent to the concentrator and vice versa
        concentrator.bind(agent);
        agent.bind(concentrator);

        // remember the agent and ControllableResource association
        agents.put(controllableResource, agent);

        logger.info("Agent bound to Concentrator and ControllableResource");
    }

    /**
     * Creates an agent with the given id, as an instance of the class which name is given and is configured with the
     * given properties (e.g. using default values from the properties).
     * 
     * @param agentId
     *            The id of the agent.
     * @param agentClass
     *            The name of the agent's class.
     * @param properties
     *            The configuration from which to use the defaults.
     * @return The configured agent.
     */
    protected FPAIAgent createAgent(String agentId, Class<? extends FPAIAgent> agentClass) {
        Map<String, Object> agentProperties = new HashMap<String, Object>(properties);

        String prefix = "agent" + ConfigurationService.SEPARATOR + agentId;
        agentProperties.put(prefix + ".class", agentClass.getName());
        agentProperties.put(prefix + ".id", agentId);
        agentProperties.put(prefix + ".matcher.id", concentrator.getId());
        agentProperties.put(prefix + ".agent.bid.log.level", "FULL_LOGGING");
        agentProperties.put(prefix + ".agent.price.log.level", "FULL_LOGGING");

        try {
            FPAIAgent agent = agentClass.newInstance();

            agent.setConfiguration(new PrefixedConfiguration(agentProperties, prefix));
            agent.bind(executorService);
            agent.bind(timeService);
            if (widget != null) {
                agent.bind(widget);
            }

            return agent;
        } catch (InstantiationException e) {
            throw new RuntimeException("Could not instantiate new agent: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Could not instantiate new agent: " + e.getMessage(), e);
        }
    }

    /**
     * Destroys the agent associated to the ControllableResource which is unregistered
     * 
     * @see Controller#registerResource(ControllableResource)
     */
    @Override
    public synchronized void unregisterResource(ControllableResource controllableResource) {
        logger.debug("Removing agent associated with ControllableResource of type " + controllableResource.getResourceType());

        // remove the agent from the list
        FPAIAgent agent = agents.remove(controllableResource);

        if (agent != null) {
            // unbind the agent from the ControllableResource
            agent.unbind(controllableResource); // also unbinds ControllableResource from Agent

            // unbind the agent from the concentrator and vice versa
            concentrator.unbind(agent);
            agent.unbind(concentrator);

            // unbind the executor service
            agent.unbind(executorService);
            agent.unbind(timeService);

            logger.info("{} unbound from Concentrator and ControllableResource", agent.getClass().getSimpleName());
        }
    }
}
