package net.powermatcher.integration.util;

import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.core.concentrator.Concentrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author FAN
 * @version 2.0
 */
public class ConcentratorWrapper
    extends Concentrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcentratorWrapper.class);

    private PriceUpdate lastPublishedPriceUpdate;
    private PriceUpdate lastReceivedPriceUpdate;
    private Bid lastReceivedBid;
    private Bid lastPublishedBid;

    /**
     * {@inheritDoc}
     */
    @Override
    public void handlePriceUpdate(PriceUpdate priceUpdate) {
        lastReceivedPriceUpdate = priceUpdate;
        super.handlePriceUpdate(priceUpdate);

        // This should reflect the check in Concentrator.updatePrice
        if (priceUpdate != null) {
            lastPublishedPriceUpdate = priceUpdate;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleBidUpdate(Session session, Bid newBid) {
        try {
            // Exceptions can be thrown in updateBid, if so, lastPublishedBid is not set.
            lastReceivedBid = newBid;
            super.handleBidUpdate(session, newBid);
            lastPublishedBid = newBid;
        } catch (IllegalArgumentException e) {
            LOGGER.error("Illegal argument or state in updateBid.", e);
            throw e;
        } catch (IllegalStateException e) {
            LOGGER.error("Illegal argument or state in updateBid.", e);
            throw e;
        }
    }

    public PriceUpdate getLastPublishedPriceUpdate() {
        return lastPublishedPriceUpdate;
    }

    public PriceUpdate getLastReceivedPriceUpdate() {
        return lastReceivedPriceUpdate;
    }

    public PriceUpdate getLastPrice() {
        return lastPublishedPriceUpdate;
    }

    public Bid getLastReceivedBid() {
        return lastReceivedBid;
    }

    public Bid getLastPublishedBid() {
        return lastPublishedBid;
    }
}
