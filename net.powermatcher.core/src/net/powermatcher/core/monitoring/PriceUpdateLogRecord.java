package net.powermatcher.core.monitoring;

import java.text.DateFormat;
import java.util.Date;

import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.api.monitoring.events.PriceUpdateEvent;

public class PriceUpdateLogRecord extends LogRecord {

    private PriceUpdate priceUpdate;

    public PriceUpdateLogRecord(PriceUpdateEvent event, Date logTime, DateFormat dateFormat) {

        super(event.getClusterId(), event.getAgentId(), event.getQualifier(), logTime, event.getTimestamp(), dateFormat);
        this.priceUpdate = event.getPriceUpdate();
    }

    public PriceUpdate getPriceUpdate() {
        return priceUpdate;
    }
}
