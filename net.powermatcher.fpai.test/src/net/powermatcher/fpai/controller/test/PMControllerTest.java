package net.powermatcher.fpai.controller.test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import junit.framework.Assert;
import net.powermatcher.core.agent.concentrator.framework.AbstractConcentrator;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.service.MatcherService;
import net.powermatcher.core.agent.marketbasis.adapter.MarketBasisAdapter;
import net.powermatcher.core.configurable.PrefixedConfiguration;
import net.powermatcher.fpai.agent.buffer.BufferAgent;
import net.powermatcher.fpai.agent.storage.StorageAgent;
import net.powermatcher.fpai.agent.timeshifter.TimeshifterAgent;
import net.powermatcher.fpai.agent.uncontrolled.UncontrolledAgent;
import net.powermatcher.fpai.controller.PMController;
import net.powermatcher.fpai.test.BidAnalyzer;
import net.powermatcher.fpai.test.MockMatcherService;
import net.powermatcher.fpai.test.MockResourceManager;

import org.flexiblepower.rai.BufferControlSpace;
import org.flexiblepower.rai.ResourceType;
import org.flexiblepower.rai.StorageControlSpace;
import org.flexiblepower.rai.TimeShifterControlSpace;
import org.flexiblepower.rai.UncontrolledLGControlSpace;
import org.flexiblepower.rai.unit.EnergyUnit;
import org.flexiblepower.rai.unit.PowerUnit;
import org.flexiblepower.rai.unit.TimeUnit;
import org.flexiblepower.rai.values.Duration;
import org.flexiblepower.rai.values.EnergyProfile;
import org.flexiblepower.rai.values.EnergyValue;
import org.flexiblepower.rai.values.PowerConstraint;
import org.flexiblepower.rai.values.PowerConstraintList;
import org.flexiblepower.rai.values.PowerValue;
import org.flexiblepower.ral.ResourceManager;
import org.flexiblepower.time.TimeService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PMControllerTest {
    private static final int WAIT_TIME = -2000;

    private final Map<String, Object> configuration;
    private final Map<ResourceManager, String> resourceMangers = new HashMap<ResourceManager, String>();

    public PMControllerTest() {
        configuration = new HashMap<String, Object>();
        configuration.put("applianceIds", "test");
        configuration.put("cluster.id", "ExampleCluster");
        configuration.put("location.id", "ExampleLocation");
        configuration.put("messaging.protocol", "INTERNAL_v1");
        configuration.put("bid.topic.suffix", "UpdateBid");
        configuration.put("price.info.topic.suffix", "UpdatePriceInfo");
        configuration.put("broker.uri", "tcp://localhost:1883");
        configuration.put("update.interval", 1);
        configuration.put("auctioneer.id", "auctioneer");
        configuration.put("market.id", "market");
        configuration.put("market.minimum.price", "0");
        configuration.put("market.maximum.price", "0.99");
        configuration.put("market.price.steps", "100");
        configuration.put("market.significance", "2");
    }

    private String concentratorId;
    private MockMatcherService mockMatcherService;
    private MarketBasisAdapter marketBasisAdapter;
    private ScheduledThreadPoolExecutor executorService;
    private PMController controller;

    @Before
    public void initController() throws Exception {
        marketBasisAdapter = new MarketBasisAdapter(new PrefixedConfiguration(configuration, "market"));
        mockMatcherService = new MockMatcherService();

        controller = new PMController() {

            @Override
            protected void createConcentratorUplink(AbstractConcentrator concentrator) throws Exception {
                concentratorId = concentrator.getId();

                marketBasisAdapter.setAgentConnector(concentrator);
                marketBasisAdapter.bind();

                concentrator.bind((MatcherService) mockMatcherService);
            }

            @Override
            protected void deactivateConcentratorUplink(AbstractConcentrator concentrator) {
                concentrator.unbind((MatcherService) mockMatcherService);
                marketBasisAdapter.unbind();
                concentratorId = null;
            }
        };

        executorService = new ScheduledThreadPoolExecutor(2);
        controller.setExecutorService(executorService);
        controller.setTimeService(new TimeService() {
            @Override
            public long getCurrentTimeMillis() {
                return System.currentTimeMillis();
            }

            @Override
            public Date getTime() {
                return new Date();
            }
        });
        controller.init(configuration);
    }

    @After
    public void tearDown() throws InterruptedException {
        controller.deactive();
        controller = null;
        marketBasisAdapter = null;
        mockMatcherService = null;

        executorService.shutdown();
        executorService.awaitTermination(1, java.util.concurrent.TimeUnit.MINUTES);
    }

    private MockResourceManager createRM(ResourceType type) {
        String applianceId = type + "-" + UUID.randomUUID().toString();
        MockResourceManager resourceManager = new MockResourceManager(applianceId, type);
        resourceMangers.put(resourceManager, applianceId);
        controller.registerResource(resourceManager);
        return resourceManager;
    }

    private void updateUncontrolledCS(MockResourceManager resourceManager, Duration duration, EnergyValue energy) {
        Date now = new Date();
        Date limit = duration.addTo(now);
        Date start = duration.removeFrom(now);
        EnergyProfile energyProfile = new EnergyProfile.Builder().setDuration(duration).add(energy).build();
        resourceManager.updateControlSpace(new UncontrolledLGControlSpace(resourceMangers.get(resourceManager),
                                                                          now,
                                                                          limit,
                                                                          limit,
                                                                          start,
                                                                          energyProfile));
    }

    private void updateBufferCS(MockResourceManager resourceManager,
                                EnergyValue totalCapacity,
                                PowerValue chargeSpeed,
                                PowerValue selfDischargeSpeed) {
        Date now = new Date();
        Date limit = new Duration(1, TimeUnit.HOURS).addTo(now);

        resourceManager.updateControlSpace(new BufferControlSpace(resourceMangers.get(resourceManager),
                                                                  now,
                                                                  limit,
                                                                  limit,
                                                                  totalCapacity,
                                                                  .5f,
                                                                  new PowerConstraintList(new PowerConstraint(new PowerValue(0,
                                                                                                                             chargeSpeed.getUnit()),
                                                                                                              chargeSpeed)),
                                                                  selfDischargeSpeed,
                                                                  Duration.ZERO,
                                                                  Duration.ZERO,
                                                                  null,
                                                                  null));
    }

    private void updateStorageCS(MockResourceManager resourceManager,
                                 EnergyValue totalCapacity,
                                 PowerValue chargeSpeed,
                                 PowerValue dischargeSpeed,
                                 PowerValue selfDischargeSpeed) {
        Date now = new Date();
        Date limit = new Duration(1, TimeUnit.HOURS).addTo(now);

        resourceManager.updateControlSpace(new StorageControlSpace(resourceMangers.get(resourceManager),
                                                                   now,
                                                                   limit,
                                                                   limit,
                                                                   totalCapacity,
                                                                   .5f,
                                                                   new PowerConstraintList(new PowerConstraint(new PowerValue(0,
                                                                                                                              chargeSpeed.getUnit()),
                                                                                                               chargeSpeed)),
                                                                   new PowerConstraintList(new PowerConstraint(new PowerValue(0,
                                                                                                                              dischargeSpeed.getUnit()),
                                                                                                               dischargeSpeed)),
                                                                   selfDischargeSpeed,
                                                                   1f,
                                                                   1f,
                                                                   Duration.ZERO,
                                                                   Duration.ZERO,
                                                                   null,
                                                                   null));
    }

    private void updateTimeShifterCS(MockResourceManager resourceManager, EnergyProfile profile) {
        Date now = new Date();
        Date limit = new Duration(1, TimeUnit.HOURS).addTo(now);

        resourceManager.updateControlSpace(new TimeShifterControlSpace(resourceMangers.get(resourceManager),
                                                                       now,
                                                                       limit,
                                                                       limit,
                                                                       profile,
                                                                       limit,
                                                                       new Duration(1, TimeUnit.MILLISECONDS).removeFrom(now)));
    }

    @Test
    public void testSingleUncontrolled() throws Exception {
        Assert.assertEquals(0, controller.getAgentList().size());

        MockResourceManager resourceManager = createRM(ResourceType.UNCONTROLLED);
        Assert.assertEquals(1, controller.getAgentList().size());
        Assert.assertEquals(UncontrolledAgent.class, controller.getAgentList().iterator().next().getClass());

        updateUncontrolledCS(resourceManager,
                             new Duration(1, TimeUnit.HOURS),
                             new EnergyValue(1, EnergyUnit.KILO_WATTHOUR));

        BidInfo bid = mockMatcherService.getLastBid(concentratorId, WAIT_TIME);
        BidAnalyzer.assertFlatBidWithValue(bid, new PowerValue(1, PowerUnit.KILO_WATT));

        controller.unregisterResource(resourceManager);
        Assert.assertEquals(0, controller.getAgentList().size());

        bid = mockMatcherService.getLastBid(concentratorId, WAIT_TIME);
        BidAnalyzer.assertFlatBidWithValue(bid, new PowerValue(0, PowerUnit.WATT));
    }

    @Test
    public void testSingleBuffer() throws Exception {
        Assert.assertEquals(0, controller.getAgentList().size());

        MockResourceManager resourceManager = createRM(ResourceType.BUFFER);
        Assert.assertEquals(1, controller.getAgentList().size());
        Assert.assertEquals(BufferAgent.class, controller.getAgentList().iterator().next().getClass());

        updateBufferCS(resourceManager,
                       new EnergyValue(5, EnergyUnit.KILO_WATTHOUR),
                       new PowerValue(1, PowerUnit.KILO_WATT),
                       new PowerValue(20, PowerUnit.WATT));

        BidInfo bid = mockMatcherService.getLastBid(concentratorId, WAIT_TIME);
        BidAnalyzer.assertNonFlatBid(bid);

        controller.unregisterResource(resourceManager);
        Assert.assertEquals(0, controller.getAgentList().size());

        bid = mockMatcherService.getLastBid(concentratorId, WAIT_TIME);
        BidAnalyzer.assertFlatBidWithValue(bid, new PowerValue(0, PowerUnit.WATT));
    }

    @Test
    public void testSingleStorage() throws Exception {
        Assert.assertEquals(0, controller.getAgentList().size());

        MockResourceManager resourceManager = createRM(ResourceType.STORAGE);
        Assert.assertEquals(1, controller.getAgentList().size());
        Assert.assertEquals(StorageAgent.class, controller.getAgentList().iterator().next().getClass());

        updateStorageCS(resourceManager,
                        new EnergyValue(5, EnergyUnit.KILO_WATTHOUR),
                        new PowerValue(1, PowerUnit.KILO_WATT),
                        new PowerValue(1, PowerUnit.KILO_WATT),
                        new PowerValue(20, PowerUnit.WATT));

        BidInfo bid = mockMatcherService.getLastBid(concentratorId, WAIT_TIME);
        BidAnalyzer.assertNonFlatBid(bid);

        controller.unregisterResource(resourceManager);
        Assert.assertEquals(0, controller.getAgentList().size());

        bid = mockMatcherService.getLastBid(concentratorId, WAIT_TIME);
        BidAnalyzer.assertFlatBidWithValue(bid, new PowerValue(0, PowerUnit.WATT));
    }

    @Test
    public void testSingleTimeShifter() throws Exception {
        Assert.assertEquals(0, controller.getAgentList().size());

        MockResourceManager resourceManager = createRM(ResourceType.TIMESHIFTER);
        Assert.assertEquals(1, controller.getAgentList().size());
        Assert.assertEquals(TimeshifterAgent.class, controller.getAgentList().iterator().next().getClass());

        updateTimeShifterCS(resourceManager, new EnergyProfile(new Duration(1, TimeUnit.HOURS),
                                                               new EnergyValue(1, EnergyUnit.KILO_WATTHOUR)));
        Thread.sleep(1);
        BidInfo bid = mockMatcherService.getLastBid(concentratorId, WAIT_TIME);
        BidAnalyzer.assertNonFlatBid(bid);

        controller.unregisterResource(resourceManager);
        Assert.assertEquals(0, controller.getAgentList().size());

        bid = mockMatcherService.getLastBid(concentratorId, WAIT_TIME);
        BidAnalyzer.assertFlatBidWithValue(bid, new PowerValue(0, PowerUnit.WATT));
    }

    @Test
    public void testMultipbleUncontrolled() throws Exception {
        MockResourceManager[] rms = new MockResourceManager[4];

        for (int ix = 0; ix < rms.length; ix++) {
            rms[ix] = createRM(ResourceType.UNCONTROLLED);
            updateUncontrolledCS(rms[ix], new Duration(1, TimeUnit.HOURS), new EnergyValue(1, EnergyUnit.KILO_WATTHOUR));

            BidInfo bid = mockMatcherService.getLastBid(concentratorId, WAIT_TIME);
            BidAnalyzer.assertFlatBidWithValue(bid, new PowerValue(ix + 1, PowerUnit.KILO_WATT));
        }

        for (int ix = 0; ix < rms.length; ix++) {
            controller.unregisterResource(rms[ix]);

            BidInfo bid = mockMatcherService.getLastBid(concentratorId, WAIT_TIME);
            BidAnalyzer.assertFlatBidWithValue(bid, new PowerValue(3 - ix, PowerUnit.KILO_WATT));
        }
    }
}
