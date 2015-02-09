package net.powermatcher.monitoring.csv;

import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;

import net.powermatcher.api.data.Price;
import net.powermatcher.api.monitoring.events.PeakShavingEvent;

/**
 * {@link PeakShavingLogRecord} is an implementation of {@link LogRecord} that stores a {@link PeakShavingEvent}.
 * 
 * @author FAN
 * @version 2.0
 */
public class PeakShavingLogRecord extends LogRecord {

    /**
     * Minimum power level applied in 'peak shaving'.
     */
    private double floor;

    /**
     * Maximum power level applied in 'peak shaving'.
     */
    private double ceiling;

    /**
     * The demand array before the peakshaving.
     */
    private double[] oldDemand;

    /**
     * The transformed demand array after the peakshaving.
     */
    private double[] newDemand;

    /**
     * The {@link Price} before the peakshaving.
     */
    private Price oldPrice;

    /**
     * The adjusted {@link Price} after the peakshaving.
     */
    private Price newPrice;

    /**
     * A constructor that creates an instance of this class.
     * 
     * @param event
     *            the {@link PeakShavingEvent} that needs to be logged
     * @param logTime
     *            the time the event was logged
     * @param dateFormat
     *            the {@link DateFormat} that will be used to log the {@link LogRecord}
     * @param floor
     *            minimum power level applied in 'peak shaving'.
     * @param ceiling
     *            maximum power level applied in 'peak shaving'.
     * @param oldDemand
     *            the demand array before the peakshaving.
     * @param newDemand
     *            the transformed demand array after the peakshaving.
     * @param newPrice
     *            the {@link Price} before the peakshaving.
     * @param oldPrice
     *            the adjusted {@link Price} after the peakshaving.
     */
    public PeakShavingLogRecord(PeakShavingEvent event, Date logTime, DateFormat dateFormat, double floor,
            double ceiling, double[] oldDemand, double[] newDemand, Price newPrice, Price oldPrice) {
        super(event.getClusterId(), event.getAgentId(), logTime, event.getTimestamp(), dateFormat);
        this.floor = floor;
        this.ceiling = ceiling;
        this.oldDemand = Arrays.copyOf(oldDemand, oldDemand.length);
        this.newDemand = Arrays.copyOf(newDemand, newDemand.length);
        this.newPrice = newPrice;
        this.oldPrice = oldPrice;
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
     * @return a copy of oldDemand.
     */
    public double[] getOldDemand() {
        return Arrays.copyOf(oldDemand, oldDemand.length);
    }

    /**
     * @return a copy of newDemand.
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
}
