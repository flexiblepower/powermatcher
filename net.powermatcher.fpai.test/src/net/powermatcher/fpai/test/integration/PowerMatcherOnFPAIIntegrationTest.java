// TODO
//package net.powermatcher.fpai.test.integration;
//
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.Dictionary;
//import java.util.Hashtable;
//import java.util.List;
//import java.util.Properties;
//import java.util.Set;
//import java.util.concurrent.ScheduledExecutorService;
//import java.util.concurrent.ScheduledThreadPoolExecutor;
//
//import junit.framework.Assert;
//import junit.framework.TestCase;
//import net.powermatcher.core.adapter.service.AdapterService;
//import net.powermatcher.core.agent.framework.data.BidInfo;
//import net.powermatcher.core.agent.marketbasis.adapter.MarketBasisAdapter;
//import net.powermatcher.core.configurable.PrefixedConfiguration;
//import net.powermatcher.core.messaging.mqttv3.Mqttv3Connection;
//import net.powermatcher.core.messaging.mqttv3.config.Mqttv3ConnectionConfiguration;
//import net.powermatcher.core.messaging.protocol.adapter.MatcherProtocolAdapter;
//import net.powermatcher.core.messaging.protocol.adapter.config.BaseAdapterConfiguration;
//import net.powermatcher.core.messaging.protocol.adapter.config.ProtocolAdapterConfiguration;
//import net.powermatcher.core.messaging.protocol.adapter.config.ProtocolAdapterConfiguration.Protocol;
//import net.powermatcher.core.object.ActiveObject;
//import net.powermatcher.core.object.config.ActiveObjectConfiguration;
//import net.powermatcher.core.object.config.IdentifiableObjectConfiguration;
//import net.powermatcher.fpai.agent.timeshifter.test.TimeShifterControlSpaceBuilder;
//import net.powermatcher.fpai.test.BidAnalyzer;
//import net.powermatcher.fpai.test.MockMatcherService;
//import net.powermatcher.fpai.test.MockResourceManager;
//import net.powermatcher.fpai.test.MockTimeService.PowerMatcherTimeServiceAdapter;
//import net.powermatcher.fpai.test.SystemTimeService;
//
//import org.apache.activemq.broker.BrokerService;
//import org.flexiblepower.rai.ResourceType;
//import org.flexiblepower.rai.unit.EnergyUnit;
//import org.flexiblepower.rai.unit.TimeUnit;
//import org.flexiblepower.rai.values.Duration;
//import org.flexiblepower.rai.values.EnergyProfile;
//import org.flexiblepower.rai.values.EnergyValue;
//import org.flexiblepower.ral.ResourceManager;
//import org.flexiblepower.time.TimeService;
//import org.osgi.framework.Bundle;
//import org.osgi.framework.BundleContext;
//import org.osgi.framework.FrameworkUtil;
//import org.osgi.framework.ServiceRegistration;
//import org.osgi.service.cm.Configuration;
//import org.osgi.service.cm.ConfigurationAdmin;
//import org.osgi.util.tracker.ServiceTracker;
//
//public class PowerMatcherOnFPAIIntegrationTest extends TestCase {
//    private static final String AUCTIONEER_ID = "auctioneer1";
//    private static final String CLUSTER_ID = "cluster1";
//
//    private BundleContext context;
//
//    private ScheduledThreadPoolExecutor executor;
//    private SystemTimeService timeService;
//
//    private BrokerService broker;
//
//    private MockMatcherService matcher;
//    private MatcherProtocolAdapter matcherProtocolAdapter;
//    private ServiceRegistration<?> timeServiceRegistration;
//    private ServiceRegistration<?> executorServiceRegistration;
//    private MarketBasisAdapter marketBasisAdapter;
//    private Mqttv3Connection mqttv3Connection;
//
//    private final List<ServiceRegistration<?>> managerRegistrations = new ArrayList<ServiceRegistration<?>>();
//
//    private ServiceTracker<ConfigurationAdmin, ConfigurationAdmin> configAdminTracker;
//
//    public void testBidsSent() throws InterruptedException {
//        // only execute when ran in an OSGi context
//        if (context == null) {
//            return;
//        }
//
//        String applianceId = "shifter-1";
//        MockResourceManager shifterResourceManager = new MockResourceManager(applianceId, ResourceType.TIMESHIFTER);
//        Dictionary<String, Object> properties = new Hashtable<String, Object>();
//        properties.put("applianceId", applianceId);
//        managerRegistrations.add(context.registerService(ResourceManager.class, shifterResourceManager, properties));
//
//        // update the control space
//        TimeShifterControlSpaceBuilder builder = new TimeShifterControlSpaceBuilder();
//        builder.setApplianceId(applianceId);
//        builder.setEnergyProfile(new EnergyProfile(new Duration(2, TimeUnit.HOURS),
//                                                   new EnergyValue(2, EnergyUnit.KILO_WATTHOUR)));
//        int twelveHours = 12 * 60 * 60 * 1000;
//        builder.setStartAfter(new Date(timeService.currentTimeMillis() - twelveHours));
//        builder.setValidFrom(new Date(timeService.currentTimeMillis() - twelveHours));
//        builder.setStartBefore(new Date(timeService.currentTimeMillis() + twelveHours));
//        builder.setExpirationTime(new Date(timeService.currentTimeMillis() + twelveHours));
//        builder.setValidThru(new Date(timeService.currentTimeMillis() + twelveHours));
//        shifterResourceManager.updateControlSpace(builder.build());
//
//        Thread.sleep(10 * 1000);
//
//        Set<String> agentIds = matcher.getAgentIds();
//        Assert.assertEquals(1, agentIds.size());
//
//        String concentratorId = agentIds.toArray(new String[agentIds.size()])[0];
//        BidInfo lastBid = matcher.getLastBid(concentratorId, 1000);
//        Assert.assertNotNull(lastBid);
//        BidAnalyzer.assertStepBid(lastBid);
//    }
//
//    @Override
//    protected void setUp() throws Exception {
//        Bundle bundle = FrameworkUtil.getBundle(this.getClass());
//
//        // only execute when ran in an OSGi context
//        if (bundle == null) {
//            return;
//        }
//
//        context = bundle.getBundleContext();
//
//        executor = new ScheduledThreadPoolExecutor(2);
//        executorServiceRegistration = context.registerService(ScheduledExecutorService.class.getName(), executor, null);
//
//        timeService = new SystemTimeService();
//        PowerMatcherTimeServiceAdapter adaptedTimeService = new PowerMatcherTimeServiceAdapter(timeService);
//        timeServiceRegistration = context.registerService(TimeService.class, adaptedTimeService, null);
//
//        setUpMqttBroker();
//        Thread.sleep(100);
//
//        setUpAuctioneer();
//        Thread.sleep(100);
//
//        setUpPMController();
//    }
//
//    private void setUpPMController() throws Exception {
//        Dictionary<String, Object> properties = new Hashtable<String, Object>();
//        properties.put("applianceIds", "shifter-1");
//        properties.put("auctioneer.id", AUCTIONEER_ID);
//        properties.put("cluster.id", CLUSTER_ID);
//        properties.put("location.id", "ExampleLocation");
//        properties.put("update.interval", "1");
//        properties.put("broker.uri", "tcp://localhost:1883");
//        properties.put(ProtocolAdapterConfiguration.PROTOCOL_PROPERTY, Protocol.INTERNAL_v1.toString());
//        properties.put(BaseAdapterConfiguration.BID_TOPIC_SUFFIX_PROPERTY,
//                       BaseAdapterConfiguration.BID_TOPIC_SUFFIX_DEFAULT);
//        properties.put(BaseAdapterConfiguration.PRICE_INFO_TOPIC_SUFFIX_PROPERTY,
//                       BaseAdapterConfiguration.PRICE_INFO_TOPIC_SUFFIX_DEFAULT);
//
//        String factoryPid = "net.powermatcher.fpai.controller.PMController";
//        Configuration configuration = getConfigAdminTracker().getService().createFactoryConfiguration(factoryPid, null);
//        configuration.update(properties);
//    }
//
//    private ServiceTracker<ConfigurationAdmin, ConfigurationAdmin> getConfigAdminTracker() throws InterruptedException {
//        if (configAdminTracker != null) {
//            return configAdminTracker;
//        }
//
//        configAdminTracker = new ServiceTracker<ConfigurationAdmin, ConfigurationAdmin>(context,
//                                                                                        ConfigurationAdmin.class.getName(),
//                                                                                        null);
//        configAdminTracker.open();
//        configAdminTracker.waitForService(100);
//        return configAdminTracker;
//    }
//
//    private void setUpMqttBroker() throws Exception {
//        broker = new BrokerService();
//        broker.setPersistent(false);
//        broker.setUseJmx(false);
//        broker.addConnector("mqtt://localhost:1883");
//        broker.startAllConnectors();
//        broker.start();
//    }
//
//    private void setUpAuctioneer() throws Exception {
//        PrefixedConfiguration configuration = constructAuctioneerConfiguration();
//
//        matcher = new MockMatcherService(configuration);
//        bindServices(matcher);
//
//        marketBasisAdapter = new MarketBasisAdapter(configuration);
//        marketBasisAdapter.setAgentConnector(matcher);
//        bindServices(marketBasisAdapter);
//
//        matcherProtocolAdapter = new MatcherProtocolAdapter(configuration);
//        matcherProtocolAdapter.setMatcherConnector(matcher);
//        bindServices(matcherProtocolAdapter);
//
//        mqttv3Connection = new Mqttv3Connection(configuration);
//        mqttv3Connection.addConnector(matcherProtocolAdapter);
//        bindServices(mqttv3Connection);
//    }
//
//    private PrefixedConfiguration constructAuctioneerConfiguration() {
//        Properties properties = new Properties();
//
//        // communication related configuration properties (protocol, bid and price topic suffix, broker uri)
//        properties.setProperty(ProtocolAdapterConfiguration.PROTOCOL_PROPERTY, Protocol.INTERNAL_v1.toString());
//        properties.setProperty(BaseAdapterConfiguration.BID_TOPIC_SUFFIX_PROPERTY,
//                               BaseAdapterConfiguration.BID_TOPIC_SUFFIX_DEFAULT);
//        properties.setProperty(BaseAdapterConfiguration.PRICE_INFO_TOPIC_SUFFIX_PROPERTY,
//                               BaseAdapterConfiguration.PRICE_INFO_TOPIC_SUFFIX_DEFAULT);
//        properties.setProperty(Mqttv3ConnectionConfiguration.BROKER_URI_PROPERTY, "tcp://localhost:1883");
//
//        // auctioneer configuration (identifier, cluster identifier, update interval)
//        String prefix = "agent." + AUCTIONEER_ID + ".";
//        properties.setProperty(prefix + IdentifiableObjectConfiguration.ID_PROPERTY, AUCTIONEER_ID);
//        properties.setProperty(prefix + IdentifiableObjectConfiguration.ENABLED_PROPERTY, "true");
//        properties.setProperty(prefix + IdentifiableObjectConfiguration.CLUSTER_ID_PROPERTY, CLUSTER_ID);
//        properties.setProperty(prefix + ActiveObjectConfiguration.UPDATE_INTERVAL_DEFAULT, "1");
//        PrefixedConfiguration configuration = new PrefixedConfiguration(properties, "agent." + AUCTIONEER_ID);
//        return configuration;
//    }
//
//    @Override
//    protected void tearDown() throws Exception {
//        // only execute when ran in an OSGi context
//        if (context == null) {
//            return;
//        }
//
//        configAdminTracker.close();
//        configAdminTracker = null;
//
//        for (ServiceRegistration<?> registration : managerRegistrations) {
//            registration.unregister();
//        }
//
//        for (ActiveObject object : new ActiveObject[] { matcher, matcherProtocolAdapter, mqttv3Connection }) {
//            unbindServices(object);
//        }
//
//        matcher = null;
//        matcherProtocolAdapter = null;
//        mqttv3Connection = null;
//
//        executorServiceRegistration.unregister();
//        executor.shutdown();
//        executor = null;
//
//        timeServiceRegistration.unregister();
//        timeService = null;
//
//        broker.stop();
//        broker = null;
//
//        context = null;
//    }
//
//    private void bindServices(ActiveObject object) throws Exception {
//        object.bind(timeService);
//        object.bind(executor);
//
//        if (object instanceof AdapterService) {
//            ((AdapterService) object).bind();
//        }
//    }
//
//    private void unbindServices(ActiveObject object) {
//        object.unbind(timeService);
//        object.unbind(executor);
//
//        if (object instanceof AdapterService) {
//            ((AdapterService) object).unbind();
//        }
//    }
// }
