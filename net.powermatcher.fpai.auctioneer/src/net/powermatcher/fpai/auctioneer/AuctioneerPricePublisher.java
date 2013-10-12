package net.powermatcher.fpai.auctioneer;

import java.io.Closeable;

import net.powermatcher.core.agent.framework.log.BidLogInfo;
import net.powermatcher.core.agent.framework.log.LogListenerService;
import net.powermatcher.core.agent.framework.log.PriceLogInfo;
import net.powermatcher.fpai.auctioneer.AuctioneerPricePublisher.Price;

import org.flexiblepower.observation.Observation;
import org.flexiblepower.observation.ext.AbstractObservationProvider;
import org.flexiblepower.observation.ext.ObservationProviderRegistrationHelper;
import org.flexiblepower.time.TimeService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class AuctioneerPricePublisher extends AbstractObservationProvider<Price> implements
                                                                                LogListenerService,
                                                                                Closeable {
    private final TimeService timeService;
    private final ServiceRegistration<?> serviceRegistration;

    public interface Price {
        double getPrice();
    }

    public AuctioneerPricePublisher(BundleContext context, TimeService timeService) {
        this.timeService = timeService;
        serviceRegistration = new ObservationProviderRegistrationHelper(this).observationOf("auctioneer")
                                                                             .observationType(Price.class)
                                                                             .observedBy(getClass().getName())
                                                                             .register();
    }

    @Override
    public void handleBidLogInfo(BidLogInfo paramBidLogInfo) {
    }

    @Override
    public void handlePriceLogInfo(final PriceLogInfo paramPriceLogInfo) {
        final double price = paramPriceLogInfo.getCurrentPrice();
        publish(new Observation<AuctioneerPricePublisher.Price>(timeService.getTime(), new Price() {
            @Override
            public double getPrice() {
                return price;
            }
        }));
    }

    @Override
    public void close() {
        serviceRegistration.unregister();
    }
}
