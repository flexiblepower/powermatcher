package net.powermatcher.core.monitoring;

import java.text.DateFormat;
import java.util.Date;

import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.monitoring.BidEvent;

public class BidLogRecord extends LogRecord {

    private Bid bid;

    public BidLogRecord(BidEvent event, Date logTime, DateFormat dateFormat) {
        super(event.getClusterId(), event.getAgentId(), event.getQualifier(), logTime, event.getTimestamp(), dateFormat);

        this.bid = event.getBid();
    }

    @Override
    public String[] getLine() {
        MarketBasis marketBasis = bid.getMarketBasis();

        return new String[] { getDateFormat().format(getLogTime()), getClusterId(), getAgentId(), getQualifier().getDescription(),
                marketBasis.getCommodity(), marketBasis.getCurrency(),
                MarketBasis.PRICE_FORMAT.format(marketBasis.getMinimumPrice()),
                MarketBasis.PRICE_FORMAT.format(marketBasis.getMaximumPrice()),
                MarketBasis.DEMAND_FORMAT.format(this.bid.getMinimumDemand()),
                MarketBasis.DEMAND_FORMAT.format(this.bid.getMaximumDemand()),
                // TODO where/what is the "effective demand"?
                MarketBasis.DEMAND_FORMAT.format(0),
                // TODO where/what is the "effective price"?
                MarketBasis.PRICE_FORMAT.format(0), getDateFormat().format(getEventTimestamp()), this.bid.toString() };
    }
}
