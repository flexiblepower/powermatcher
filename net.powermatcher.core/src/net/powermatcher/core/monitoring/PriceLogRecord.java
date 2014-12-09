package net.powermatcher.core.monitoring;

import java.text.DateFormat;
import java.util.Date;

import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.monitoring.PriceEvent;

public class PriceLogRecord extends LogRecord {

    private Price price;

    public PriceLogRecord(PriceEvent event, Date logTime, DateFormat dateFormat) {
        
        super(event.getClusterId(), event.getAgentId(), event.getQualifier(), logTime, event.getTimestamp(), dateFormat);
        this.price = event.getPrice();
    }

    @Override
    public String[] getLine() {
        MarketBasis marketbasis = price.getMarketBasis();

        return new String[] { getDateFormat().format(getLogTime()), getClusterId(), getAgentId(), getQualifier().getDescription(),
                marketbasis.getCommodity(), marketbasis.getCurrency(),
                MarketBasis.PRICE_FORMAT.format(marketbasis.getMinimumPrice()),
                MarketBasis.PRICE_FORMAT.format(marketbasis.getMaximumPrice()),
                MarketBasis.PRICE_FORMAT.format(price.getPriceValue()), getDateFormat().format(getEventTimestamp()) };
    }
}
