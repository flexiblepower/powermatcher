package net.powermatcher.fpai.agent;

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

public abstract class FPAIAgent<CS extends ControlSpace> extends Agent implements Controller<CS> {

    private CS currentControlSpace;
    private ControllableResource<? extends ControlSpace> controllableResource;

    protected FPAIAgent() {
        super();
    }

    protected FPAIAgent(ConfigurationService configuration) {
        super(configuration);
    }

    protected abstract BidInfo createBid(CS controlSpace, MarketBasis marketBasis);

    protected abstract Allocation createAllocation(BidInfo lastBid, PriceInfo newPriceInfo, CS controlSpace);

    @Override
    protected synchronized void doBidUpdate() {
        // force a bid update based on the current control space
        ControlSpace controlSpace = getCurrentControlSpace();
        if (controlSpace != null) {
            // controlSpaceUpdated(controllableResource, controlSpace);
        }
    }

    @Override
    public synchronized void controlSpaceUpdated(ControllableResource<? extends CS> resource, CS controlSpace) {
        assert controllableResource == resource;
        assert controlSpace != null;

        BidInfo bidInfo;
        if (controlSpace.getValidThru().getTime() > getTimeSource().currentTimeMillis()) {
            // Control space hasn't expired

            // remember the updated control space
            setCurrentControlSpace(controlSpace);

            // calculate a new bid
            bidInfo = createBid(controlSpace, getCurrentMarketBasis());
            this.logDebug("Control space was updated (" + controlSpace + "), triggereing updating the bid: " + bidInfo);
        } else {
            // There is no valid control space left. We assume this indicates that there is no flexibility.
            bidInfo = new BidInfo(getCurrentMarketBasis(), new PricePoint(0, 0));
            this.logDebug("No valid control space found, triggering must-off bid");
        }
        // and publish it
        publishBidUpdate(bidInfo);

        // FIXME it is not guaranteed that the price is updated
        // however this is assumed for now for demonstration purposes
    }

    @Override
    public void updatePriceInfo(PriceInfo newPriceInfo) {
        super.updatePriceInfo(newPriceInfo);

        // check if there is control space information available
        CS currentControlSpace = getCurrentControlSpace();
        if (currentControlSpace == null) {
            this.logDebug("Ignoring price update, no control space information available");
            return;
        }

        BidInfo lastBid = getLastBid();
        if (lastBid == null) {
            this.logDebug("Ignoring price update, bid no bid published yet");
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

    public synchronized void bind(ControllableResource<CS> controllableResource) {
        assert controllableResource == null;
        this.controllableResource = controllableResource;
        controllableResource.setController(this);
    }

    public synchronized void unbind(ControllableResource<CS> controllableResource) {
        // send out 0 bid to say bye-bye
        publishBidUpdate(new BidInfo(getCurrentMarketBasis(), new PricePoint(0, 0)));

        assert this.controllableResource == controllableResource;
        controllableResource.unsetController(this);
        setCurrentControlSpace(null);
        controllableResource = null;
    }

    private CS getCurrentControlSpace() {
        if (controllableResource == null) {
            return null;
        }

        synchronized (controllableResource) {
            return currentControlSpace;
        }
    }

    private void setCurrentControlSpace(CS controlSpace) {
        synchronized (controllableResource) {
            currentControlSpace = controlSpace;
        }
    }
}
