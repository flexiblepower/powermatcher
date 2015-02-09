package net.powermatcher.test.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.mock.MockAgent;
import net.powermatcher.mock.MockMatcherAgent;
import net.powermatcher.mock.MockScheduler;
import net.powermatcher.mock.MockTimeService;
import net.powermatcher.mock.SimpleSession;

public class TestClusterHelper
    implements Closeable, Iterable<MockAgent> {
    public static final MarketBasis DEFAULT_MB = new MarketBasis("electricity", "EUR", 11, 0, 10);

    private final AtomicInteger idGenerator;

    private final MockScheduler scheduler;
    private final MockTimeService timer;

    private final List<MockAgent> agents;
    private final List<SimpleSession> sessions;

    private final MarketBasis marketBasis;
    private final MatcherEndpoint matcher;

    public TestClusterHelper() {
        this(DEFAULT_MB);
    }

    public TestClusterHelper(MarketBasis marketBasis) {
        this(marketBasis, null);
    }

    public TestClusterHelper(MatcherEndpoint matcher) {
        this(DEFAULT_MB, matcher);
    }

    public TestClusterHelper(MarketBasis marketBasis, MatcherEndpoint matcher) {
        idGenerator = new AtomicInteger(0);

        scheduler = new MockScheduler();
        timer = new MockTimeService(0);

        agents = new ArrayList<MockAgent>();
        sessions = new ArrayList<SimpleSession>();

        this.marketBasis = marketBasis;
        this.matcher = matcher;

        if (matcher != null) {
            matcher.setExecutorService(scheduler);
            matcher.setTimeService(timer);
        }
    }

    public MatcherEndpoint getMatcher() {
        if (matcher == null) {
            throw new IllegalStateException("Matcher has not been set");
        }
        return matcher;
    }

    public MockAgent addAgent() {
        return addAgent(getMatcher());
    }

    public MockAgent addAgent(MatcherEndpoint matcher) {
        return addAgents(matcher, 1).get(0);
    }

    public List<MockAgent> addAgents(int nrOfAgents) {
        return addAgents(getMatcher(), nrOfAgents);
    }

    public List<MockAgent> addAgents(MatcherEndpoint matcher, int nrOfAgents) {
        List<MockAgent> newAgents = new ArrayList<MockAgent>(nrOfAgents);
        for (int ix = 0; ix < nrOfAgents; ix++) {
            String agentId = "agent" + idGenerator.incrementAndGet();
            MockAgent newAgent = new MockAgent(agentId);
            newAgent.setDesiredParentId(matcher.getAgentId());
            agents.add(newAgent);
            newAgents.add(newAgent);

            connect(newAgent, matcher);
        }
        return newAgents;
    }

    public void connect(AgentEndpoint agent, MatcherEndpoint matcher) {
        SimpleSession session = new SimpleSession(agent, matcher);
        session.connect();
        sessions.add(session);
    }

    public void sendBid(int agentIx, int bidNr, double... demandArray) {
        agents.get(agentIx).sendBid(new ArrayBid(marketBasis, bidNr, demandArray));
    }

    public void sendBids(int baseId, double[]... demandArrays) {
        for (int ix = 0; ix < demandArrays.length; ix++) {
            sendBid(ix, baseId + ix, demandArrays[ix]);
        }
        scheduler.doTaskOnce();
    }

    public void testPriceSignal(MockMatcherAgent matcher, int... expectedIds) {
        Price price = new Price(marketBasis, Math.random() * marketBasis.getMaximumPrice());
        matcher.publishPrice(new PriceUpdate(price, matcher.getLastReceivedBid().getBidNumber()));
        for (int i = 0; i < expectedIds.length; i++) {
            MockAgent agent = agents.get(i);
            if (expectedIds[i] < 0) {
                assertNull(agent.getLastPriceUpdate());
            } else {
                assertEquals(price, agent.getLastPriceUpdate().getPrice());
                assertEquals(expectedIds[i], agent.getLastPriceUpdate().getBidNumber());
            }
        }
    }

    @Override
    public void close() {
        for (SimpleSession session : sessions) {
            session.disconnect();
        }
        sessions.clear();
        agents.clear();
    }

    public MockAgent getAgent(int ix) {
        if (agents.size() <= ix) {
            addAgents(agents.size() - ix + 1);
        }
        return agents.get(ix);
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public MarketBasis getMarketBasis() {
        return marketBasis;
    }

    public void performTasks() {
        scheduler.doTaskOnce();
    }

    public TimeService getTimer() {
        return timer;
    }

    public double[] getPriceValues() {
        double[] prices = new double[agents.size()];
        for (int ix = 0; ix < agents.size(); ix++) {
            PriceUpdate priceUpdate = agents.get(ix).getLastPriceUpdate();
            if (priceUpdate == null) {
                prices[ix] = Double.POSITIVE_INFINITY;
            } else {
                prices[ix] = priceUpdate.getPrice().getPriceValue();
            }
        }
        return prices;
    }

    @Override
    public Iterator<MockAgent> iterator() {
        return Collections.unmodifiableList(agents).iterator();
    }
}
