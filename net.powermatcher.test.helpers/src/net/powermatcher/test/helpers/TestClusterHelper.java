package net.powermatcher.test.helpers;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.data.ArrayBid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.mock.MockAgent;
import net.powermatcher.mock.MockContext;
import net.powermatcher.mock.MockMatcherAgent;
import net.powermatcher.mock.SimpleSession;

public class TestClusterHelper
    implements Closeable, Iterable<MockAgent> {
    public static final MarketBasis DEFAULT_MB = new MarketBasis("electricity", "EUR", 11, 0, 10);

    private final AtomicInteger idGenerator;

    private final MockContext context;

    private final List<MockAgent> agents;
    private final List<SimpleSession> sessions;

    private final MarketBasis marketBasis;
    private final MatcherEndpoint matcher;

    public TestClusterHelper(MatcherEndpoint matcher) {
        this(DEFAULT_MB, matcher);
    }

    public TestClusterHelper(MarketBasis marketBasis, MatcherEndpoint matcher) {
        if (matcher == null) {
            throw new NullPointerException("matcher");
        }
        idGenerator = new AtomicInteger(0);

        context = new MockContext(0);

        agents = new ArrayList<MockAgent>();
        sessions = new ArrayList<SimpleSession>();

        this.marketBasis = marketBasis;
        this.matcher = matcher;

        if (matcher != null) {
            matcher.setContext(context);
        }
    }

    public MockAgent addAgent() {
        return addAgents(1).get(0);
    }

    public List<MockAgent> addAgents(int nrOfAgents) {
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
        getAgent(agentIx).sendBid(new ArrayBid(marketBasis, demandArray), bidNr);
    }

    public void sendBids(int baseId, double[]... demandArrays) {
        for (int ix = 0; ix < demandArrays.length; ix++) {
            sendBid(ix, baseId + ix, demandArrays[ix]);
        }
        performTasks();
    }

    public void testPriceSignal(MockMatcherAgent matcher, int... expectedIds) {
        Price price = new Price(marketBasis, Math.random() * marketBasis.getMaximumPrice());
        matcher.publishPrice(new PriceUpdate(price, matcher.getLastReceivedBid().getBidNumber()));
        for (int i = 0; i < expectedIds.length; i++) {
            MockAgent agent = getAgent(i);
            if (expectedIds[i] < 0) {
                if (agent.getLastPriceUpdate() == null) {
                    throw new AssertionError("Last price update of agent " + i + " is null");
                }
            } else {
                if (!agent.getLastPriceUpdate().getPrice().equals(price)) {
                    throw new AssertionError("expected: " + price + " got: " + agent.getLastPriceUpdate().getPrice());
                }
                if (agent.getLastPriceUpdate().getBidNumber() != expectedIds[i]) {
                    throw new AssertionError("expected: " + expectedIds[i]
                                             + "  got: "
                                             + agent.getLastPriceUpdate().getBidNumber());
                }
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

    public MarketBasis getMarketBasis() {
        return marketBasis;
    }

    public void performTasks() {
        context.doTaskOnce();
    }

    public List<PriceUpdate> getPriceUpdates() {
        List<PriceUpdate> updates = new ArrayList<PriceUpdate>(agents.size());
        for (MockAgent agent : agents) {
            updates.add(agent.getLastPriceUpdate());
        }
        return updates;
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
