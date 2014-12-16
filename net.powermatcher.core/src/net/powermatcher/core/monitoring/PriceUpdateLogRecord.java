package net.powermatcher.core.monitoring;

import java.text.DateFormat;
import java.util.Date;

import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.api.monitoring.Qualifier;
import net.powermatcher.api.monitoring.events.AgentEvent;
import net.powermatcher.api.monitoring.events.PriceUpdateEvent;

public class PriceUpdateLogRecord extends LogRecord {

    private PriceUpdate priceUpdate;
    /**
     * The qualifier of the agent that published the {@link AgentEvent}.
     */
    private Qualifier qualifier;


    public PriceUpdateLogRecord(PriceUpdateEvent event, Date logTime, DateFormat dateFormat) {

        super(event.getClusterId(), event.getAgentId(), logTime, event.getTimestamp(), dateFormat);
        this.priceUpdate = event.getPriceUpdate();
        this.qualifier = event.getQualifier();
    }

    protected PriceUpdate getPriceUpdate() {
        return priceUpdate;
    }
    
    public Qualifier getQualifier() {
        return qualifier;
    }
}
