package net.powermatcher.api.monitoring.events;

import java.util.Arrays;
import java.util.Date;

import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;

public class PeakShavingEvent extends AgentEvent {

    private double floor;
    private double ceiling;
    private double[] oldDemand;
    private double[] newDemand;
    private Price oldPrice;
    private Price newPrice;

    public PeakShavingEvent(String agentId, String clusterId, Date timestamp, double floor, double ceiling,
            double[] oldDemand, double[] newDemand, Price newPrice, Price oldPrice) {
        super(clusterId, agentId, timestamp);
        this.floor = floor;
        this.ceiling = ceiling;
        this.oldDemand = Arrays.copyOf(oldDemand, oldDemand.length);
        this.newDemand = Arrays.copyOf(newDemand, newDemand.length);
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
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

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();

        double[] oldDemandArray = getOldDemand();
        if (oldDemandArray.length > 0) {
            sb.append(", oldDemandArray[]{");
            for (int i = 0; i < oldDemandArray.length; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(MarketBasis.DEMAND_FORMAT.format(oldDemandArray[i]));
            }
            sb.append("}, ");
        }

        double[] newDemandArray = getNewDemand();
        if (oldDemandArray.length > 0) {
            sb.append(", newDemandArray[]{");
            for (int i = 0; i < newDemandArray.length; i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append(MarketBasis.DEMAND_FORMAT.format(newDemandArray[i]));
            }
            sb.append("}, ");
        }
        //TODO add Price to toString
        return super.toString() + ", floor = " + floor + ", ceiling = " + ceiling + sb.toString();
    }
}
