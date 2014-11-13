package net.powermatcher.core.monitoring;

import java.text.DateFormat;
import java.util.Date;

import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.monitoring.AgentEvent;
import net.powermatcher.api.monitoring.IncomingPriceEvent;
import net.powermatcher.api.monitoring.OutgoingPriceEvent;

public class PriceLogRecord extends LogRecord {

    private Price price;

    public PriceLogRecord(AgentEvent event, Date logTime, DateFormat dateFormat, Price price, String qualifier) {
        // TODO you can't be sure this is a Price*Event, but unless you're going to cast in this constructor, it will be
        // fine. Create a "PriceEvent" baseclass?
        super(event.getClusterId(), event.getAgentId(), qualifier, logTime, event.getTimestamp(), dateFormat);

        this.price = price;
    }

    @Override
    public String[] getLine() {
        MarketBasis marketbasis = price.getMarketBasis();

        return new String[] { getDateFormat().format(getLogTime()), getClusterId(), getAgentId(), getQualifier(),
                marketbasis.getCommodity(), marketbasis.getCurrency(),
                MarketBasis.PRICE_FORMAT.format(marketbasis.getMinimumPrice()),
                MarketBasis.PRICE_FORMAT.format(marketbasis.getMaximumPrice()),
                MarketBasis.PRICE_FORMAT.format(price.getCurrentPrice()), getDateFormat().format(getEventTimestamp()) };
    }
l}
