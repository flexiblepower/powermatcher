package net.powermatcher.fpai.controller;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import net.powermatcher.core.agent.framework.Agent;
import net.powermatcher.core.agent.framework.log.BidLogInfo;
import net.powermatcher.core.agent.framework.log.PriceLogInfo;

public class PMWidgetImpl implements PMWidget {
    private final PMController controller;

    private final Map<String, BidLogInfo> latestBids;
    private volatile PriceLogInfo latestPrice;

    public PMWidgetImpl(PMController controller) {
        this.controller = controller;
        latestBids = new ConcurrentHashMap<String, BidLogInfo>();
        latestPrice = null;
    }

    @Override
    public void handleBidLogInfo(BidLogInfo bidLogInfo) {
        latestBids.put(bidLogInfo.getAgentId(), bidLogInfo);
    }

    @Override
    public void handlePriceLogInfo(PriceLogInfo priceLogInfo) {
        latestPrice = priceLogInfo;
    }

    public Update update(Locale locale) {
        Update update = new Update(latestPrice == null ? 0 : latestPrice.getCurrentPrice(),
                                   latestPrice == null ? "No price received yet"
                                                      : DateFormat.getTimeInstance(DateFormat.LONG, locale)
                                                                  .format(latestPrice.getTimestamp()));

        for (Agent agent : controller.getAgentList()) {
            BidLogInfo lastBid = latestBids.get(agent.getId());
            String type = getAgentLabel(agent);
            String demands = lastBid == null ? "No bid done yet" : getDemands(lastBid);
            update.addAgent(type, demands);
        }

        return update;
    }

    private String getDemands(BidLogInfo lastBid) {
        SortedSet<Double> result = new TreeSet<Double>();
        for (double demand : lastBid.getBidInfo().getDemand()) {
            result.add(demand);
        }
        if (result.size() > 3) {
            double min = result.first();
            double max = result.last();
            return "(" + min + "-" + max + ") W";
        } else {
            return result.toString() + " W";
        }
    }

    private String getAgentLabel(Agent agent) {
        String name = agent.getClass().getSimpleName();
        return name.replaceAll("(.)(\\p{Upper})", "$1 $2");
    }

    @Override
    public String getTitle(Locale locale) {
        return "PowerMatcher Controller";
    }

    public static class Update {
        private final List<String> agentTypes;
        private final List<String> demands;
        private final String marketPrice;
        private final String timestamp;

        public Update(double price, String timestamp) {
            marketPrice = String.format("%1.2f", price);
            this.timestamp = timestamp;
            agentTypes = new ArrayList<String>();
            demands = new ArrayList<String>();
        }

        public void addAgent(String type, String demand) {
            agentTypes.add(type);
            demands.add(demand);
        }

        public List<String> getAgentTypes() {
            return agentTypes;
        }

        public List<String> getDemands() {
            return demands;
        }

        public String getMarketPrice() {
            return marketPrice;
        }

        public String getTimestamp() {
            return timestamp;
        }
    }
}
