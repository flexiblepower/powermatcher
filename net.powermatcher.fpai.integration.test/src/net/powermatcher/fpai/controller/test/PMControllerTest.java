package net.powermatcher.fpai.controller.test;

import static javax.measure.unit.NonSI.HOUR;
import static javax.measure.unit.NonSI.KWH;
import static javax.measure.unit.SI.KILO;
import static javax.measure.unit.SI.MILLI;
import static javax.measure.unit.SI.SECOND;
import static javax.measure.unit.SI.WATT;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import junit.framework.Assert;
import junit.framework.TestCase;
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
import org.flexiblepower.rai.ControlSpace;
import org.flexiblepower.rai.StorageControlSpace;
import org.flexiblepower.rai.TimeShifterControlSpace;
import org.flexiblepower.rai.UncontrolledControlSpace;
import org.flexiblepower.rai.values.ConstraintList;
import org.flexiblepower.rai.values.EnergyProfile;
import org.flexiblepower.ral.ResourceManager;
import org.flexiblepower.time.TimeService;
import org.flexiblepower.time.TimeUtil;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class PMControllerTest extends TestCase {
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

    private BundleContext bundleContext;

    @Override
    public void setUp() throws Exception {
        bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();

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
        controller.init(bundleContext, configuration);
    }

    @Override
    public void tearDown() throws InterruptedException {
        controller.deactive();
        controller = null;
        marketBasisAdapter = null;
        mockMatcherService = null;

        executorService.shutdown();
        executorService.awaitTermination(1, java.util.concurrent.TimeUnit.MINUTES);
    }

    private MockResourceManager createRM(Class<? extends ControlSpace> type) {
        String resourceId = type.getName() + "-" + UUID.randomUUID().toString();
        MockResourceManager resourceManager = new MockResourceManager(resourceId, type);
        resourceMangers.put(resourceManager, resourceId);
        controller.registerResource(resourceManager);
        return resourceManager;
    }

    private void updateUncontrolledCS(MockResourceManager resourceManager,
                                      Measurable<Duration> duration,
                                      Measurable<Energy> energy) {
        Date start = new Date();
        EnergyProfile energyProfile = EnergyProfile.create().add(duration, energy).build();
        resourceManager.updateControlSpace(new UncontrolledControlSpace(resourceMangers.get(resourceManager),
                                                                        start,
                                                                        energyProfile));
    }

    private void updateBufferCS(MockResourceManager resourceManager,
                                Measurable<Energy> totalCapacity,
                                Measurable<Power> chargeSpeed,
                                Measurable<Power> selfDischargeSpeed) {
        Date now = new Date();
        Date limit = TimeUtil.add(now, Measure.valueOf(1, HOUR));

        resourceManager.updateControlSpace(new BufferControlSpace(resourceMangers.get(resourceManager),
                                                                  now,
                                                                  limit,
                                                                  limit,
                                                                  totalCapacity,
                                                                  .5f,
                                                                  ConstraintList.create(WATT)
                                                                                .addSingle(0)
                                                                                .addSingle(chargeSpeed)
                                                                                .build(),
                                                                  selfDischargeSpeed,
                                                                  Measure.valueOf(0, SECOND),
                                                                  Measure.valueOf(0, SECOND),
                                                                  null,
                                                                  null));
    }

    private void updateStorageCS(MockResourceManager resourceManager,
                                 Measurable<Energy> totalCapacity,
                                 Measurable<Power> chargeSpeed,
                                 Measurable<Power> dischargeSpeed,
                                 Measurable<Power> selfDischargeSpeed) {
        Date now = new Date();
        Date limit = TimeUtil.add(now, Measure.valueOf(1, HOUR));

        resourceManager.updateControlSpace(new StorageControlSpace(resourceMangers.get(resourceManager),
                                                                   now,
                                                                   limit,
                                                                   limit,
                                                                   totalCapacity,
                                                                   .5f,
                                                                   ConstraintList.create(WATT)
                                                                                 .addSingle(0)
                                                                                 .addSingle(chargeSpeed)
                                                                                 .build(),
                                                                   ConstraintList.create(WATT)
                                                                                 .addSingle(0)
                                                                                 .addSingle(dischargeSpeed)
                                                                                 .build(),
                                                                   selfDischargeSpeed,
                                                                   1f,
                                                                   1f,
                                                                   Measure.valueOf(0, SECOND),
                                                                   Measure.valueOf(0, SECOND),
                                                                   null,
                                                                   null));
    }

    private void updateTimeShifterCS(MockResourceManager resourceManager, EnergyProfile profile) {
        Date now = new Date();
        Date limit = TimeUtil.add(now, Measure.valueOf(1, HOUR));

        resourceManager.updateControlSpace(new TimeShifterControlSpace(resourceMangers.get(resourceManager),
                                                                       now,
                                                                       limit,
                                                                       limit,
                                                                       profile,
                                                                       limit,
                                                                       TimeUtil.subtract(now,
                                                                                         Measure.valueOf(1,
                                                                                                         MILLI(SECOND)))));
    }

    public void testSingleUncontrolled() throws Exception {
        Assert.assertEquals(0, controller.getAgentList().size());

        MockResourceManager resourceManager = createRM(UncontrolledControlSpace.class);
        Assert.assertEquals(1, controller.getAgentList().size());
        Assert.assertEquals(UncontrolledAgent.class, controller.getAgentList().iterator().next().getClass());

        updateUncontrolledCS(resourceManager, Measure.valueOf(1, HOUR), Measure.valueOf(1, KWH));

        BidInfo bid = mockMatcherService.getLastBid(concentratorId, WAIT_TIME);
        BidAnalyzer.assertFlatBidWithValue(bid, Measure.valueOf(1, KILO(WATT)));

        controller.unregisterResource(resourceManager);
        Assert.assertEquals(0, controller.getAgentList().size());

        bid = mockMatcherService.getLastBid(concentratorId, WAIT_TIME);
        BidAnalyzer.assertFlatBidWithValue(bid, Measure.valueOf(0, WATT));
    }

    public void testSingleBuffer() throws Exception {
        Assert.assertEquals(0, controller.getAgentList().size());

        MockResourceManager resourceManager = createRM(BufferControlSpace.class);
        Assert.assertEquals(1, controller.getAgentList().size());
        Assert.assertEquals(BufferAgent.class, controller.getAgentList().iterator().next().getClass());

        updateBufferCS(resourceManager,
                       Measure.valueOf(5, KWH),
                       Measure.valueOf(1, KILO(WATT)),
                       Measure.valueOf(20, WATT));

        BidInfo bid = mockMatcherService.getLastBid(concentratorId, WAIT_TIME);
        BidAnalyzer.assertNonFlatBid(bid);

        controller.unregisterResource(resourceManager);
        Assert.assertEquals(0, controller.getAgentList().size());

        bid = mockMatcherService.getLastBid(concentratorId, WAIT_TIME);
        BidAnalyzer.assertFlatBidWithValue(bid, Measure.valueOf(0, WATT));
    }

    public void testSingleStorage() throws Exception {
        Assert.assertEquals(0, controller.getAgentList().size());

        MockResourceManager resourceManager = createRM(StorageControlSpace.class);
        Assert.assertEquals(1, controller.getAgentList().size());
        Assert.assertEquals(StorageAgent.class, controller.getAgentList().iterator().next().getClass());

        updateStorageCS(resourceManager,
                        Measure.valueOf(5, KWH),
                        Measure.valueOf(1, KILO(WATT)),
                        Measure.valueOf(1, KILO(WATT)),
                        Measure.valueOf(20, WATT));

        BidInfo bid = mockMatcherService.getLastBid(concentratorId, WAIT_TIME);
        BidAnalyzer.assertNonFlatBid(bid);

        controller.unregisterResource(resourceManager);
        Assert.assertEquals(0, controller.getAgentList().size());

        bid = mockMatcherService.getLastBid(concentratorId, WAIT_TIME);
        BidAnalyzer.assertFlatBidWithValue(bid, Measure.valueOf(0, WATT));
    }

    public void testSingleTimeShifter() throws Exception {
        Assert.assertEquals(0, controller.getAgentList().size());

        MockResourceManager resourceManager = createRM(TimeShifterControlSpace.class);
        Assert.assertEquals(1, controller.getAgentList().size());
        Assert.assertEquals(TimeshifterAgent.class, controller.getAgentList().iterator().next().getClass());

        updateTimeShifterCS(resourceManager,
                            EnergyProfile.create().add(Measure.valueOf(1, HOUR), Measure.valueOf(1, KWH)).build());
        Thread.sleep(1);
        BidInfo bid = mockMatcherService.getLastBid(concentratorId, WAIT_TIME);
        BidAnalyzer.assertNonFlatBid(bid);

        controller.unregisterResource(resourceManager);
        Assert.assertEquals(0, controller.getAgentList().size());

        bid = mockMatcherService.getLastBid(concentratorId, WAIT_TIME);
        BidAnalyzer.assertFlatBidWithValue(bid, Measure.valueOf(0, WATT));
    }

    public void testMultipbleUncontrolled() throws Exception {
        MockResourceManager[] rms = new MockResourceManager[4];

        for (int ix = 0; ix < rms.length; ix++) {
            rms[ix] = createRM(UncontrolledControlSpace.class);
            updateUncontrolledCS(rms[ix], Measure.valueOf(1, HOUR), Measure.valueOf(1, KWH));

            BidInfo bid = mockMatcherService.getLastBid(concentratorId, WAIT_TIME);
            BidAnalyzer.assertFlatBidWithValue(bid, Measure.valueOf(ix + 1, KILO(WATT)));
        }

        for (int ix = 0; ix < rms.length; ix++) {
            controller.unregisterResource(rms[ix]);

            BidInfo bid = mockMatcherService.getLastBid(concentratorId, WAIT_TIME);
            BidAnalyzer.assertFlatBidWithValue(bid, Measure.valueOf(3 - ix, KILO(WATT)));
        }
    }
}
