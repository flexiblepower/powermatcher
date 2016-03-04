package net.powermatcher.integration.util;

import net.powermatcher.api.Session;
import net.powermatcher.api.messages.BidUpdate;
import net.powermatcher.api.messages.PriceUpdate;
import net.powermatcher.core.concentrator.Concentrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author FAN
 * @version 2.1
 */
public class ConcentratorWrapper
    extends Concentrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcentratorWrapper.class);

    private PriceUpdate lastPublishedPriceUpdate;
    private PriceUpdate lastReceivedPriceUpdate;
    private BidUpdate lastReceivedBid;
    private BidUpdate lastPublishedBid;

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
    public void handleBidUpdate(Session session, BidUpdate bidUpdate) {
        try {
            // Exceptions can be thrown in updateBid, if so, lastPublishedBid is not set.
            lastReceivedBid = bidUpdate;
            super.handleBidUpdate(session, bidUpdate);
            lastPublishedBid = bidUpdate;
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

    public BidUpdate getLastReceivedBid() {
        return lastReceivedBid;
    }

    public BidUpdate getLastPublishedBid() {
        return lastPublishedBid;
    }
}
