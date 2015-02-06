package net.powermatcher.test.helpers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.Closeable;
import java.util.ArrayList;
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
    implements Closeable {
    public static final MarketBasis MB = new MarketBasis("electricity", "EUR", 11, 0, 10);

    private final AtomicInteger idGenerator;

    private final MockScheduler scheduler;
    private final MockTimeService timer;

    private final List<MockAgent> agents;
    private final List<SimpleSession> sessions;

    public TestClusterHelper() {
        idGenerator = new AtomicInteger(0);

        scheduler = new MockScheduler();
        timer = new MockTimeService(0);

        agents = new ArrayList<MockAgent>();
        sessions = new ArrayList<SimpleSession>();
    }

    public void addAgent(MatcherEndpoint matcher) {
        addAgents(matcher, 1);
    }

    public void addAgents(MatcherEndpoint matcher, int nrOfAgents) {
        for (int ix = 0; ix < nrOfAgents; ix++) {
            String agentId = "agent" + idGenerator.incrementAndGet();
            MockAgent newAgent = new MockAgent(agentId);
            newAgent.setDesiredParentId(matcher.getAgentId());
            agents.add(newAgent);

            connect(newAgent, matcher);
        }
    }

    public void connect(AgentEndpoint agent, MatcherEndpoint matcher) {
        SimpleSession session = new SimpleSession(agent, matcher);
        session.connect();
        sessions.add(session);
    }

    public void sendBid(int agentIx, int bidNr, double... demandArray) {
        agents.get(agentIx).sendBid(new ArrayBid(MB, bidNr, demandArray));
    }

    public void sendBids(int baseId, double[]... demandArrays) {
        for (int ix = 0; ix < demandArrays.length; ix++) {
            sendBid(ix, baseId + ix, demandArrays[ix]);
        }
        scheduler.doTaskOnce();
    }

    public void testPriceSignal(MockMatcherAgent matcher, int... expectedIds) {
        Price price = new Price(MB, Math.random() * MB.getMaximumPrice());
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

    public ScheduledExecutorService getScheduler() {
        return scheduler;
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
}
