package net.powermatcher.api.monitoring.events;

import java.util.Arrays;
import java.util.Date;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;

/**
 * An {@link PeakShavingEvent} is sent when an PeakShavingConcentrator transforms an aggregated {@link Bid} by
 * peakshaving the demandArray.
 *
 * @author FAN
 * @version 2.0
 */
public class PeakShavingEvent
    extends AgentEvent {

    /**
     * The minimum power level applied in peak shaving.
     */
    private final double floor;

    /**
     * The maximum power level applied in peak shaving.
     */
    private final double ceiling;

    /**
     * The demand array before the peakshaving.
     */
    private final double[] oldDemand;

    /**
     * The demand array after the peakshaving.
     */
    private final double[] newDemand;

    /**
     * The {@link Price} before the peakshaving.
     */
    private final Price oldPrice;

    /**
     * The {@link Price} after the peakshaving.
     */
    private final Price newPrice;

    /**
     * A constructor to create an instance of a PeakShavingEvent
     *
     * @param agentId
     *            The id of the {@link AgentEndpoint} subclass sending the UpdateEvent.
     * @param clusterId
     *            The id of the cluster the {@link AgentEndpoint} subclass sending the UpdateEvent is running in.
     * @param timestamp
     *            The {@link Date} received from the {@link TimeService}
     * @param floor
     *            The minimum power level applied in peak shaving.
     * @param ceiling
     *            The maximum power level applied in peak shaving.
     * @param oldDemand
     *            The demand array before the peakshaving.
     * @param newDemand
     *            The demand array after the peakshaving.
     * @param newPrice
     *            The {@link Price} before the peakshaving.
     * @param oldPrice
     *            The {@link Price} after the peakshaving.
     */
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

    /**
     * @return the current value of floor.
     */
    public double getFloor() {
        return floor;
    }

    /**
     * @return the current value of ceiling.
     */
    public double getCeiling() {
        return ceiling;
    }

    /**
     * @return a copy of the current oldDemand.
     */
    public double[] getOldDemand() {
        return Arrays.copyOf(oldDemand, oldDemand.length);
    }

    /**
     * @returna copy of the current newDemand.
     */
    public double[] getNewDemand() {
        return Arrays.copyOf(newDemand, newDemand.length);
    }

    /**
     * @return the current value of oldPrice.
     */
    public Price getOldPrice() {
        return oldPrice;
    }

    /**
     * @return the current value of newPrice.
     */
    public Price getNewPrice() {
        return newPrice;
    }

    /**
     * {@inheritDoc}
     */
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
        // TODO add Price to toString
        return super.toString() + ", floor = " + floor + ", ceiling = " + ceiling + sb.toString();
    }
}
