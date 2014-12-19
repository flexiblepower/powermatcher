package net.powermatcher.integration.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.core.concentrator.Concentrator;

public class ConcentratorWrapper extends Concentrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConcentratorWrapper.class);

    private PriceUpdate lastPublishedPriceUpdate;
    private PriceUpdate lastReceivedPriceUpdate;
    private Bid lastReceivedBid;
    private Bid lastPublishedBid;

    @Override
    public void updatePrice(PriceUpdate priceUpdate) {
        this.lastReceivedPriceUpdate = priceUpdate;
        super.updatePrice(priceUpdate);

        // This should reflect the check in Concentrator.updatePrice
        if (priceUpdate != null) {
            this.lastPublishedPriceUpdate = priceUpdate;
        }
    }

    @Override
    public void updateBid(Session session, Bid newBid) {
        try {
            // Exceptions can be thrown in updateBid, if so, lastPublishedBid is not set.
            this.lastReceivedBid = newBid;
            super.updateBid(session, newBid);
            this.lastPublishedBid = newBid;
        } catch (IllegalArgumentException | IllegalStateException e) {
            LOGGER.error("Illegal argument or state in updateBid.", e);
            throw e;
        }
    }

    public synchronized void doBidUpdate() {
        super.doBidUpdate();
    }

    public PriceUpdate getLastPublishedPriceUpdate() {
        return this.lastPublishedPriceUpdate;
    }

    public PriceUpdate getLastReceivedPriceUpdate() {
        return this.lastReceivedPriceUpdate;
    }

    public PriceUpdate getLastPrice() {
        return this.lastPublishedPriceUpdate;
    }

    public Bid getLastReceivedBid() {
        return this.lastReceivedBid;
    }

    public Bid getLastPublishedBid() {
        return this.lastPublishedBid;
    }
}
