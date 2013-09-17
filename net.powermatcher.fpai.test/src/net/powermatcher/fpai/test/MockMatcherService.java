package net.powermatcher.fpai.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import net.powermatcher.core.agent.framework.MatcherAgent;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.configurable.service.ConfigurationService;

/**
 * Implementation of the MatcherService interface which allows for retrieval of the last bid of an agent sent via the
 * matcher service interface.
 */
public class MockMatcherService extends MatcherAgent {
    private final Map<String, List<BidInfo>> allBids = new HashMap<String, List<BidInfo>>();

    public MockMatcherService() {
        super();
    }

    public MockMatcherService(ConfigurationService configuration) {
        super(configuration);
    }

    @Override
    public void updateBidInfo(String agentId, BidInfo newBidInfo) {
        List<BidInfo> agentBids = getAgentBids(agentId);

        // add the new bid and notify any thread waiting on this bid
        synchronized (agentBids) {
            agentBids.add(newBidInfo);
            agentBids.notifyAll();
        }
    }

    /**
     * Returns the last known bid of an agent with the given id.
     * 
     * @param agentId
     *            The id of the agent for which to retrieve the last bid.
     * @return The last bid of the agent or null if no such bid is available .
     */
    public BidInfo getLastBid(String agentId) {
        return getLastBid(agentId, 0);
    }

    /**
     * Returns the last known bid of an agent with the given id. The method blocks for the timeout (in milliseconds) if
     * such a bid is not yet known. If the blocking thread is interrupted, this method will return the last bid if know
     * or null if not.
     * 
     * @param agentId
     *            The id of the agent for which to retrieve the last bid.
     * @param timeout
     *            The maximum number of milliseconds to wait for the bid.
     * @return The last bid of the agent or null if no such bid is available within the given timeout.
     */
    public BidInfo getLastBid(String agentId, long timeout) {
        List<BidInfo> agentBids = getAgentBids(agentId);

        synchronized (agentBids) {
            // if empty wait for a bid to be added (which unblocks the wait)
            // or the timeout expires or this thread is interrupted
            if (timeout < 0 || agentBids.isEmpty() && timeout > 0) {
                try {
                    agentBids.wait(Math.abs(timeout));
                } catch (InterruptedException e) {
                    // swallow
                }
            }

            // if there still are no bids, return null
            if (agentBids.isEmpty()) {
                return null;
            }

            // return the last bid and clear the list
            BidInfo lastBid = agentBids.remove(agentBids.size() - 1);
            agentBids.clear();
            return lastBid;
        }
    }

    /**
     * @return The list of known bids for an agent with the given agent id which is memorized in the bids field. Never
     *         returns null.
     */
    private List<BidInfo> getAgentBids(String agentId) {
        synchronized (allBids) {
            List<BidInfo> agentBids = allBids.get(agentId);

            // if there is no list of bids for this agent, add it to the map
            if (agentBids == null) {
                agentBids = new CopyOnWriteArrayList<BidInfo>();
                allBids.put(agentId, agentBids);
            }

            return agentBids;
        }
    }

    /** @return Returns the list of known agent identifiers (those that have sent a bid before). */
    public Set<String> getAgentIds() {
        synchronized (allBids) {
            return allBids.keySet();
        }
    }
}
