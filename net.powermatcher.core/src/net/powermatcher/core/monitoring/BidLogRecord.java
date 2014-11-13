package net.powermatcher.core.monitoring;

import java.text.DateFormat;
import java.util.Date;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.monitoring.AgentEvent;

public class BidLogRecord extends LogRecord {

    private Bid bid;

    public BidLogRecord(AgentEvent event, Date logTime, DateFormat dateFormat, Bid bid, String qualifier) {
        // TODO you can't be sure this is a Bid*Event, but unless you're going to cast in this constructor, it will be
        // fine. Create a "BaseEvent" baseclass?
        super(event.getClusterId(), event.getAgentId(), qualifier, logTime, event.getTimestamp(), dateFormat);

        this.bid = bid;
    }

    @Override
    public String[] getLine() {
        MarketBasis marketBasis = bid.getMarketBasis();

        return new String[] { getDateFormat().format(getLogTime()), getClusterId(), getAgentId(), getQualifier(),
                marketBasis.getCommodity(), marketBasis.getCurrency(),
                MarketBasis.PRICE_FORMAT.format(marketBasis.getMinimumPrice()),
                MarketBasis.PRICE_FORMAT.format(marketBasis.getMaximumPrice()),
                MarketBasis.DEMAND_FORMAT.format(this.bid.getMinimumDemand()),
                MarketBasis.DEMAND_FORMAT.format(this.bid.getMaximumDemand()),
                // TODO where is the "effective demand"?
                MarketBasis.DEMAND_FORMAT.format(0),
                // TODO where is the "effective price"?
                MarketBasis.PRICE_FORMAT.format(0), getDateFormat().format(getEventTimestamp()), bid.toString() };
    }
}
