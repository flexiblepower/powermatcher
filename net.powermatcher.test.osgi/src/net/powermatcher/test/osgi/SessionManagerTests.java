package net.powermatcher.test.osgi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;
import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.core.BaseAgentEndpoint;
import net.powermatcher.core.BaseMatcherEndpoint;
import net.powermatcher.core.bidcache.AggregatedBid;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionManagerTests
    extends TestCase {
    static final Logger LOGGER = LoggerFactory.getLogger(SessionManagerTests.class);
    static final String MATCHER_ID = "testmatcher";
    static final MarketBasis MB = new MarketBasis("Electricity", "EUR", 10, 0, 1);

    private static class TestAgent
        extends BaseAgentEndpoint {
        private static final AtomicInteger agentCounter = new AtomicInteger();

        private final ServiceRegistration<AgentEndpoint> serviceRegistration;
        private volatile boolean receivedPrice = false;

        public TestAgent(BundleContext bundleContext) {
            init("testagent-" + agentCounter.incrementAndGet(), MATCHER_ID);
            serviceRegistration = bundleContext.registerService(AgentEndpoint.class, this, null);
        }

        public void close() {
            serviceRegistration.unregister();
        }

        public boolean join() {
            try {
                synchronized (this) {
                    if (!receivedPrice) {
                        wait(1000);
                    }
                    if (!receivedPrice) {
                        LOGGER.error("Agent [{}] did not receive a priceupdate", getAgentId());
                    }
                    return receivedPrice;
                }
            } catch (InterruptedException ex) {
                return receivedPrice;
            } finally {
                close();
            }
        }

        @Override
        public synchronized void connectToMatcher(Session session) {
            super.connectToMatcher(session);
            context.submit(new Runnable() {
                @Override
                public void run() {
                    publishBid(Bid.flatDemand(getStatus().getMarketBasis(), Math.random() * 100));
                }
            });
        }

        @Override
        public void handlePriceUpdate(PriceUpdate priceUpdate) {
            super.handlePriceUpdate(priceUpdate);
            synchronized (this) {
                receivedPrice = true;
                notifyAll();
            }
        }
    }

    private static class TestMatcher
        extends BaseMatcherEndpoint {
        private final ServiceRegistration<MatcherEndpoint> serviceRegistration;

        public TestMatcher(BundleContext bundleContext) {
            init(MATCHER_ID);
            configure(MB, "testcluster", 0);
            serviceRegistration = bundleContext.registerService(MatcherEndpoint.class, this, null);
        }

        public void close() {
            unconfigure();
            serviceRegistration.unregister();
        }

        @Override
        protected void performUpdate(AggregatedBid aggregatedBid) {
            publishPrice(new Price(MB, 0.5), aggregatedBid);
        }
    };

    private BundleContext bundleContext;

    @Override
    protected void setUp() throws Exception {
        bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
    }

    public void testSimple() throws InterruptedException {
        TestMatcher testMatcher = new TestMatcher(bundleContext);
        TestAgent testAgent = new TestAgent(bundleContext);

        testAgent.join();
        testMatcher.close();
    }

    private static class AgentCreator
        extends Thread {
        private static final AtomicInteger threadCounter = new AtomicInteger();

        private final int count;
        private final BundleContext bundleContext;

        private final AtomicInteger failed;

        public AgentCreator(BundleContext bundleContext, int count) {
            super("Creating " + count + " agents, thread " + threadCounter.incrementAndGet());
            this.bundleContext = bundleContext;
            this.count = count;
            failed = new AtomicInteger();
        }

        @Override
        public void run() {
            List<TestAgent> agents = new ArrayList<SessionManagerTests.TestAgent>(count);
            for (int ix = 0; ix < count; ix++) {
                agents.add(new TestAgent(bundleContext));
            }
            for (TestAgent agent : agents) {
                if (!agent.join()) {
                    failed.incrementAndGet();
                }
            }
        }

        public int joinAndGetFailedAgents() {
            try {
                join();
            } catch (InterruptedException ex) {
                // Ignore
            }
            if (isAlive()) {
                throw new AssertionError("Could not joint creation thread");
            }
            return failed.get();
        }
    }

    public void testConcurrent() throws InterruptedException {
        TestMatcher testMatcher = new TestMatcher(bundleContext);

        List<AgentCreator> creators = new ArrayList<SessionManagerTests.AgentCreator>();
        int THREAD_COUNT = 20, AGENTS_PER_THREAD = 20;
        for (int ix = 0; ix < THREAD_COUNT; ix++) {
            creators.add(new AgentCreator(bundleContext, AGENTS_PER_THREAD));
        }
        for (AgentCreator creator : creators) {
            creator.start();
        }
        int totalFailed = 0;
        for (AgentCreator creator : creators) {
            totalFailed += creator.joinAndGetFailedAgents();
        }

        assertEquals("Some of the agents did not receive a price update", 0, totalFailed);

        testMatcher.close();
    }
}
