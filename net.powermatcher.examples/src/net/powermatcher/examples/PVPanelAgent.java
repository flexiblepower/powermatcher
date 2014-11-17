package net.powermatcher.examples;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.powermatcher.api.AgentRole;
import net.powermatcher.api.Session;
import net.powermatcher.api.TimeService;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PricePoint;
import net.powermatcher.api.monitoring.IncomingPriceEvent;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.api.monitoring.OutgoingBidEvent;
import net.powermatcher.core.BaseAgent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

@Component(designateFactory = PVPanelAgent.Config.class, immediate = true, provide = { ObservableAgent.class,
        AgentRole.class })
public class PVPanelAgent extends BaseAgent implements AgentRole {

    private static final Logger LOGGER = LoggerFactory.getLogger(PVPanelAgent.class);

    public static interface Config {
        @Meta.AD(deflt = "concentrator")
        String desiredParentId();

        @Meta.AD(deflt = "pvpanel")
        String agentId();

        @Meta.AD(deflt = "30", description = "Number of seconds between bid updates")
        long bidUpdateRate();
    }

    private ScheduledFuture<?> scheduledFuture;

    private ScheduledExecutorService scheduler;

    private Session session;

    private TimeService timeService;

    @Activate
    public void activate(Map<String, Object> properties) {
        Config config = Configurable.createConfigurable(Config.class, properties);
        this.setAgentId(config.agentId());
        this.setDesiredParentId(config.desiredParentId());
        
        scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                doBidUpdate();
            }
        }, 0, config.bidUpdateRate(), TimeUnit.SECONDS);

        LOGGER.info("Agent [{}], activated", config.agentId());
    }

    @Deactivate
    public void deactivate() {
        if (session != null) {
            session.disconnect();
        }

        scheduledFuture.cancel(false);

        LOGGER.info("Agent [{}], deactivated", this.getAgentId());
    }

    protected void doBidUpdate() {
        if (session != null) {
            if (session.getMarketBasis() != null) {
                Bid newBid = new Bid(session.getMarketBasis(), new PricePoint(0, -700));
                LOGGER.debug("updateBid({})", newBid);
                session.updateBid(newBid);
                this.publishEvent(new OutgoingBidEvent(this.getAgentId(), session.getSessionId(), timeService.currentDate(),
                        newBid));
            }
        }
    }

    @Override
    public void updatePrice(Price newPrice) {
        LOGGER.debug("updatePrice({})", newPrice);
        publishEvent(new IncomingPriceEvent(this.getAgentId(), session.getSessionId(), timeService.currentDate(), newPrice));

        LOGGER.debug("Received price update [{}]", newPrice);
    }

    @Override
    public void connectToMatcher(Session session) {
        this.session = session;
    }

    @Override
    public void matcherRoleDisconnected(Session session) {
        this.session = null;
    }

    @Reference
    public void setScheduler(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    @Reference
    public void setTimeService(TimeService timeService) {
        this.timeService = timeService;
    }
}
