package net.powermatcher.test.osgi;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import junit.framework.TestCase;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.core.auctioneer.Auctioneer;
import net.powermatcher.core.concentrator.Concentrator;
import net.powermatcher.examples.Freezer;
import net.powermatcher.examples.PVPanelAgent;
import net.powermatcher.examples.StoringObserver;
import net.powermatcher.test.helpers.PropertiesBuilder;

import org.apache.felix.scr.Component;
import org.apache.felix.scr.ScrService;
import org.junit.Assert;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.util.tracker.ServiceTracker;

/**
 * Helper class which contains basic functionality used in most tests.
 * 
 * @author FAN
 * @version 2.0
 */
public class ClusterHelper
    implements ServiceListener {

    private static final int MAX_TIMEOUT = 10000; // 10 seconds

    public static final MarketBasis DEFAULT_MARKETBASIS = new MarketBasis("electricity", "EUR", 100, 0, 1);

    public static final String FACTORY_PID_AUCTIONEER = Auctioneer.class.getName();
    public static final String FACTORY_PID_CONCENTRATOR = Concentrator.class.getName();
    public static final String FACTORY_PID_PV_PANEL = PVPanelAgent.class.getName();
    public static final String FACTORY_PID_FREEZER = Freezer.class.getName();
    public static final String FACTORY_PID_OBSERVER = StoringObserver.class.getName();

    public static final String AGENT_ID_AUCTIONEER = "auctioneer";
    public static final String AGENT_ID_CONCENTRATOR = "concentrator";
    public static final String AGENT_ID_PV_PANEL = "pvPanel";
    public static final String AGENT_ID_FREEZER = "freezer";

    private final BundleContext context;

    private final ServiceTracker<ScrService, ScrService> scrServiceTracker;
    private final ScrService scrService;

    private final ServiceTracker<ConfigurationAdmin, ConfigurationAdmin> adminServiceTracker;
    private final ConfigurationAdmin admin;

    private final Set<ServiceReference<?>> savedReferences = new HashSet<ServiceReference<?>>();

    public ClusterHelper(BundleContext context) throws InterruptedException, IOException, InvalidSyntaxException {
        this.context = context;
        context.addServiceListener(this);

        scrServiceTracker = new ServiceTracker<ScrService, ScrService>(context, ScrService.class, null);
        scrServiceTracker.open();
        scrService = scrServiceTracker.waitForService(MAX_TIMEOUT);
        Assert.assertNotNull(scrService);

        adminServiceTracker = new ServiceTracker<ConfigurationAdmin, ConfigurationAdmin>(context,
                                                                                         ConfigurationAdmin.class,
                                                                                         null);
        adminServiceTracker.open();
        admin = adminServiceTracker.waitForService(MAX_TIMEOUT);
        Assert.assertNotNull(admin);

        // Cleanup running agents to start with clean test
        Configuration[] configs = admin.listConfigurations(null);
        if (configs != null) {
            for (Configuration config : configs) {
                try {
                    config.delete();
                } catch (IllegalStateException e) {
                    // Conifig already deleted by other means.
                }
            }

            // Wait a small time for all components to shutdown
            Thread.sleep(1000);
        }
    }

    public void close() {
        adminServiceTracker.close();
        scrServiceTracker.close();
        context.removeServiceListener(this);
        for (Iterator<ServiceReference<?>> it = savedReferences.iterator(); it.hasNext();) {
            context.ungetService(it.next());
            it.remove();
        }
    }

    public Configuration createConfiguration(String factoryPid, PropertiesBuilder builder) throws IOException {
        Configuration config = admin.createFactoryConfiguration(factoryPid, null);
        config.update(builder.buildDict());
        return config;
    }

    // Auctioneer creation helpers

    public PropertiesBuilder createAuctioneerProperties(String agentId, int minTimeBetweenPriceUpdates) {
        return new PropertiesBuilder().agentId(agentId)
                                      .clusterId("DefaultCluster")
                                      .marketBasis(DEFAULT_MARKETBASIS)
                                      .minTimeBetweenPriceUpdates(minTimeBetweenPriceUpdates);
    }

    public Configuration createAuctioneer(int minTimeBetweenPriceUpdates) throws IOException {
        return createAuctioneer(AGENT_ID_AUCTIONEER, minTimeBetweenPriceUpdates);
    }

    public Configuration createAuctioneer(String agentId, int minTimeBetweenPriceUpdates) throws IOException {
        return createConfiguration(FACTORY_PID_AUCTIONEER,
                                   createAuctioneerProperties(agentId, minTimeBetweenPriceUpdates));
    }

    // Concentrator creation helpers
    public PropertiesBuilder createConcentratorProperties(String agentId,
                                                          String desiredParentId,
                                                          int minTimeBetweenBidUpdates) throws IOException {
        return new PropertiesBuilder().agentId(agentId)
                                      .desiredParentId(desiredParentId)
                                      .minTimeBetweenBidUpdates(minTimeBetweenBidUpdates);
    }

    public Configuration createConcentrator(int minTimeBetweenBidUpdates) throws IOException {
        return createConcentrator(AGENT_ID_CONCENTRATOR, AGENT_ID_AUCTIONEER, minTimeBetweenBidUpdates);
    }

    public Configuration createConcentrator(String agentId,
                                            String desiredParentId,
                                            int minTimeBetweenBidUpdates) throws IOException {
        return createConfiguration(FACTORY_PID_CONCENTRATOR,
                                   createConcentratorProperties(agentId, desiredParentId, minTimeBetweenBidUpdates));
    }

    // PV Panel helpers

    public PropertiesBuilder createPvPanelProperties(String agentId, String desiredParentId, int bidUpdateRate) {
        return new PropertiesBuilder().agentId(agentId)
                                      .desiredParentId(desiredParentId)
                                      .add("bidUpdateRate", bidUpdateRate)
                                      .add("minimumDemand", -700)
                                      .add("maximumDemand", -600);
    }

    public Configuration createPvPanel(int bidUpdateRate) throws IOException {
        return createPvPanel(AGENT_ID_PV_PANEL, AGENT_ID_CONCENTRATOR, bidUpdateRate);
    }

    public Configuration createPvPanel(String agentId, String desiredParentId, int bidUpdateRate) throws IOException {
        return createConfiguration(FACTORY_PID_PV_PANEL,
                                   createPvPanelProperties(agentId, desiredParentId, bidUpdateRate));
    }

    // Freezer helpers

    public PropertiesBuilder createFreezerProperties(String agentId, String desiredParentId, int bidUpdateRate) {
        return new PropertiesBuilder().agentId(agentId)
                                      .desiredParentId(desiredParentId)
                                      .add("bidUpdateRate", bidUpdateRate)
                                      .add("minimumDemand", 1000)
                                      .add("maximumDemand", 1210);
    }

    public Configuration createFreezer(int bidUpdateRate) throws IOException {
        return createFreezer(AGENT_ID_FREEZER, AGENT_ID_CONCENTRATOR, bidUpdateRate);
    }

    public Configuration createFreezer(String agentId, String desiredParentId, int bidUpdateRate) throws IOException {
        return createConfiguration(FACTORY_PID_FREEZER,
                                   createFreezerProperties(agentId, desiredParentId, bidUpdateRate));
    }

    // Helpers for Storing Observer

    public PropertiesBuilder getStoringObserverProperties() {
        return new PropertiesBuilder().add("observableAgent_filter", "");
    }

    public Configuration createStoringObserver() throws IOException {
        return createConfiguration(FACTORY_PID_OBSERVER, getStoringObserverProperties());
    }

    public synchronized Component getComponent(final String pid) throws InterruptedException {
        long deadline = System.currentTimeMillis() + MAX_TIMEOUT;
        while (System.currentTimeMillis() < deadline) {
            Component[] components = scrService.getComponents();
            for (Component comp : components) {
                Object componentPid = comp.getProperties().get(Constants.SERVICE_PID);
                if (pid.equals(componentPid)) {
                    return comp;
                }
            }
            wait(100);
        }

        throw new AssertionError("Could not find a component for pid " + pid);
    }

    public synchronized void waitForComponentToBecomeActive(final String pid) throws InterruptedException {
        Component component = getComponent(pid);

        long deadline = System.currentTimeMillis() + MAX_TIMEOUT;
        while (System.currentTimeMillis() < deadline) {
            if (component.getState() == Component.STATE_ACTIVE) {
                return;
            }
            wait(100);
        }
        throw new AssertionError("Component for pid " + pid + " never became active");
    }

    public synchronized void waitForService(Configuration configuration) throws InterruptedException {
        String pid = configuration.getPid();
        String filter = "(" + Constants.SERVICE_PID + "=" + pid + ")";

        try {
            long deadline = System.currentTimeMillis() + MAX_TIMEOUT;
            while (System.currentTimeMillis() < deadline) {
                ServiceReference<?>[] references = context.getServiceReferences((String) null, filter);
                if (references != null) {
                    return;
                }
                wait(MAX_TIMEOUT);
            }
        } catch (InvalidSyntaxException ex) {
            throw new AssertionError("Invalid filted to find a pid " + pid, ex);
        }

        throw new AssertionError("The service with pid " + pid + "never came online");
    }

    @SuppressWarnings("unchecked")
    public synchronized <T> T getServiceByPid(Configuration configuration) throws InterruptedException {
        String pid = configuration.getPid();
        String filter = "(" + Constants.SERVICE_PID + "=" + pid + ")";

        try {
            long deadline = System.currentTimeMillis() + MAX_TIMEOUT;
            while (System.currentTimeMillis() < deadline) {
                ServiceReference<?>[] references = context.getServiceReferences((String) null, filter);
                if (references != null) {
                    ServiceReference<?> reference = references[0];
                    savedReferences.add(reference);
                    return (T) context.getService(reference);
                }
                wait(MAX_TIMEOUT);
            }
        } catch (InvalidSyntaxException ex) {
            throw new AssertionError("Invalid filted to find a pid " + pid, ex);
        }

        throw new AssertionError("The service with pid " + pid + "never came online");
    }

    public void disconnectAgent(ConfigurationAdmin configAdmin, String agentPid) throws Exception,
                                                                                InvalidSyntaxException {
        Configuration config = configAdmin.getConfiguration(agentPid);
        if (config == null) {
            TestCase.fail("Config for agent " + agentPid + " does not exist, but should be.");
        }

        config.delete();
    }

    @Override
    public synchronized void serviceChanged(ServiceEvent event) {
        // Some service changed, notify all waiting stuff
        notifyAll();
    }
}
