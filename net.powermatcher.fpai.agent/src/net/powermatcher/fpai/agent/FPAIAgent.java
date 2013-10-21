package net.powermatcher.fpai.agent;

import static javax.measure.unit.SI.MILLI;
import static javax.measure.unit.SI.SECOND;

import java.util.Date;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.Measurable;
import javax.measure.Measure;
import javax.measure.quantity.Duration;
import javax.measure.unit.NonSI;

import net.powermatcher.core.agent.framework.Agent;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.agent.framework.data.PricePoint;
import net.powermatcher.core.configurable.service.ConfigurationService;

import org.flexiblepower.rai.Allocation;
import org.flexiblepower.rai.ControlSpace;
import org.flexiblepower.rai.ControllableResource;
import org.flexiblepower.rai.Controller;

/**
 * Abstract class for PowerMatcher agents on the FPAI framework. FPAIAgents are created by the PMController
 * 
 * @author TNO
 * 
 * @param <CS>
 *            The type of ControlSpace this agent represents
 */
public abstract class FPAIAgent<CS extends ControlSpace> extends Agent implements Controller<CS>, Runnable {

    private static Measurable<Duration> UPDATE_INTERVAL = Measure.valueOf(1, NonSI.MINUTE);

    /** Last known ControlSpace */
    private CS lastControlSpace = null;
    /** Last known PriceInfo */
    private PriceInfo lastPriceInfo = null;
    /** Time when we received the last PriceInfo */
    private Date lastPriceDate = null;
    /** The resource this agent is controlling */
    private ControllableResource<? extends CS> controllableResource = null;

    private ScheduledFuture<?> scheduledFuture;

    protected FPAIAgent() {
        super();
    }

    protected FPAIAgent(ConfigurationService configuration) {
        super(configuration);
    }

    protected abstract BidInfo createBid(CS controlSpace, MarketBasis marketBasis);

    protected abstract Allocation createAllocation(BidInfo lastBid, PriceInfo newPriceInfo, CS controlSpace);

    /**
     * Force a bid update based on the current control space
     */
    @Override
    protected synchronized void doBidUpdate() {
        CS controlSpace = getLastControlSpace();
        if (controlSpace != null) {
            this.controlSpaceUpdated(this.controllableResource, controlSpace);
        }
    }

    @Override
    public synchronized void controlSpaceUpdated(ControllableResource<? extends CS> resource, CS controlSpace) {
        assert controllableResource == resource;

        BidInfo bidInfo;
        if (controlSpace == null) {
            // No flexibility available
            bidInfo = BidUtil.zeroBid(getCurrentMarketBasis());
            this.logDebug("ControlSpace was null, triggering must-off bid");
        } else if (controlSpace.getValidThru().getTime() > getTimeSource().currentTimeMillis()) {
            // Control space hasn't expired

            // remember the updated control space
            setLastControlSpace(controlSpace);

            // calculate a new bid
            bidInfo = createBid(controlSpace, getCurrentMarketBasis());
            this.logDebug("Control space was updated (" + controlSpace + "), triggering updating the bid: " + bidInfo);
        } else {
            // There is no valid control space left. We assume this indicates that there is no flexibility.
            bidInfo = BidUtil.zeroBid(getCurrentMarketBasis());
            this.logDebug("No valid control space found, triggering must-off bid");
        }
        // and publish it
        publishBidUpdate(bidInfo);
    }

    @Override
    protected void startPeriodicTasks() {
        super.startPeriodicTasks();
        scheduledFuture = getScheduler().scheduleAtFixedRate(this,
                                                             0,
                                                             UPDATE_INTERVAL.longValue(MILLI(SECOND)),
                                                             TimeUnit.MILLISECONDS);
    }

    @Override
    protected void stopPeriodicTasks() {
        super.stopPeriodicTasks();
        scheduledFuture.cancel(false);
    }

    @Override
    public void updatePriceInfo(PriceInfo newPriceInfo) {
        super.updatePriceInfo(newPriceInfo);
        synchronized (this) {
            this.lastPriceInfo = newPriceInfo;
            this.lastPriceDate = new Date(getTimeSource().currentTimeMillis());
        }

        // check if there is control space information available
        CS currentControlSpace = getLastControlSpace();
        if (currentControlSpace == null) {
            this.logDebug("Ignoring price update, no control space information available");
            return;
        }

        BidInfo lastBid = getLastBid();
        if (lastBid == null) {
            this.logDebug("Ignoring price update, no bid published yet");
            return;
        }

        // if so, construct a new allocation and send it the ControllableResource
        Allocation allocation = createAllocation(lastBid, newPriceInfo, currentControlSpace);
        if (allocation != null) {
            controllableResource.handleAllocation(allocation);

            this.logDebug("Price update (" + newPriceInfo.getCurrentPrice()
                          + ") triggered calculation of new allocation: "
                          + allocation);
        } else {
            this.logDebug("Price update (" + newPriceInfo.getCurrentPrice()
                          + ") received, but no allocation was calculated");
        }
    }

    /**
     * Bind this controller to a controllableResource
     * 
     * @param controllableResource
     */
    public synchronized void bind(ControllableResource<CS> controllableResource) {
        assert this.controllableResource == null;
        this.controllableResource = controllableResource;
        controllableResource.setController(this);
    }

    /**
     * Unbind this controller from a controllableResource
     * 
     * @param controllableResource
     */
    public synchronized void unbind(ControllableResource<CS> controllableResource) {
        // send out 0 bid to say bye-bye
        publishBidUpdate(new BidInfo(getCurrentMarketBasis(), new PricePoint(0, 0)));

        assert this.controllableResource == controllableResource;
        controllableResource.unsetController(this);
        setLastControlSpace(null);
        controllableResource = null;
    }

    private CS getLastControlSpace() {
        if (controllableResource == null) {
            return null;
        }

        synchronized (controllableResource) {
            return lastControlSpace;
        }
    }

    private void setLastControlSpace(CS controlSpace) {
        synchronized (controllableResource) {
            lastControlSpace = controlSpace;
        }
    }

    /**
     * PowerMatcher doesn't guarantee price updates. A new ControlSpace triggers a bid update; a price update can
     * trigger an allocation. Since price updates aren't guaranteed by PowerMatcher, we have to make sure that the
     * updatePriceInfo method is called once in a while so the agent always has an opportunity to create allocations.
     */
    @Override
    public void run() {
        PriceInfo priceInfo;
        Date priceDate;
        synchronized (this) {
            priceInfo = lastPriceInfo;
            priceDate = lastPriceDate;
        }
        if (priceInfo != null && priceDate != null) {
            if (priceDate.getTime() + UPDATE_INTERVAL.longValue(MILLI(SECOND)) < getTimeSource().currentTimeMillis()) {
                // We haven't received a new PriceInfo in the last UPDATE_INTERVAL, resubmit the last one
                this.updatePriceInfo(priceInfo);
            }
        }
    }
}
