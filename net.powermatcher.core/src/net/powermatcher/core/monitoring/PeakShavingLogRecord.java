package net.powermatcher.core.monitoring;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;

import net.powermatcher.api.data.Price;
import net.powermatcher.api.monitoring.events.AgentEvent;

public class PeakShavingLogRecord extends LogRecord {

    private double floor;
    private double ceiling;
    private double[] oldDemand;
    private double[] newDemand;
    private Price oldPrice;
    private Price newPrice;

    public PeakShavingLogRecord(AgentEvent event, Date logTime, DateFormat dateFormat, double floor, double ceiling,
            double[] oldDemand, double[] newDemand, Price newPrice, Price oldPrice) {
        super(event.getClusterId(), event.getAgentId(), logTime, event.getTimestamp(), dateFormat);
        this.floor = floor;
        this.ceiling = ceiling;
        this.oldDemand = Arrays.copyOf(oldDemand, oldDemand.length);
        this.newDemand = Arrays.copyOf(newDemand, newDemand.length);
        this.newPrice = newPrice;
        this.oldPrice = oldPrice;
    }

    public double getFloor() {
        return floor;
    }

    public double getCeiling() {
        return ceiling;
    }

    public double[] getOldDemand() {
        return Arrays.copyOf(oldDemand, oldDemand.length);
    }

    public double[] getNewDemand() {
        return Arrays.copyOf(newDemand, newDemand.length);
    }

    public Price getOldPrice() {
        return oldPrice;
    }

    public Price getNewPrice() {
        return newPrice;
    }
}
