package net.powermatcher.fpai.auctioneer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;

import net.powermatcher.core.agent.auctioneer.Auctioneer;
import net.powermatcher.core.agent.framework.service.AgentConnectorService;
import net.powermatcher.core.agent.framework.service.AgentService;
import net.powermatcher.core.agent.marketbasis.adapter.MarketBasisAdapter;
import net.powermatcher.core.configurable.BaseConfiguration;

import org.flexiblepower.time.TimeService;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta.AD;
import aQute.bnd.annotation.metatype.Meta.OCD;

@Component(immediate = true, designate = AuctioneerManager.Config.class)
public class AuctioneerManager {
    private static final Logger logger = LoggerFactory.getLogger(AuctioneerManager.class);

    private static final String AGENT_FILTER_KEY = "auctioneer.id";

    @OCD
    interface Config {
        @AD(deflt = "ExampleCluster")
        String cluster_id();

        @AD(deflt = "ExampleLocation")
        String location_id();

        @AD(deflt = "1")
        int update_interval();

        @AD(deflt = "auctioneer1")
        String auctioneer_id();

        @AD(deflt = "0")
        double minimum_price();

        @AD(deflt = "100")
        double maximum_price();

        @AD(deflt = "FULL_LOGGING")
        String matcher_price_log_level();

        @AD(deflt = "3")
        int significance();
    }

    private Auctioneer auctioneer;

    private final Set<AgentService> agents = new HashSet<AgentService>();

    private Config config;

    private AuctioneerPricePublisher publisher;

    @Activate
    public synchronized void activate(BundleContext context, Map<String, Object> properties) {
        publisher = new AuctioneerPricePublisher(context, timeService);

        config = Configurable.createConfigurable(Config.class, properties);
        HashMap<String, Object> newProperties = new HashMap<String, Object>(properties);
        newProperties.put("id", config.auctioneer_id());
        BaseConfiguration matcherConfiguration = new BaseConfiguration(newProperties);

        auctioneer = new Auctioneer(matcherConfiguration);
        marketBasisAdapter = new MarketBasisAdapter(matcherConfiguration);
        marketBasisAdapter.setAgentConnector(auctioneer);
        marketBasisAdapter.bind(executorService);
        marketBasisAdapter.bind(pmTimeService);
        try {
            marketBasisAdapter.bind();
        } catch (Exception e1) {
        }

        auctioneer.bind(executorService);
        auctioneer.bind(pmTimeService);
        auctioneer.bind(publisher);

        logger.debug("Initialized auctioneer with " + newProperties);

        // Bind initial agents
        logger.debug("Adding agents: " + agents);
        Iterator<AgentService> it = agents.iterator();
        while (it.hasNext()) {
            AgentService agent = it.next();
            if (match(properties)) {
                auctioneer.bind(agent);
                try {
                    ((AgentConnectorService) agent).bind(auctioneer);
                } catch (Exception e) {
                }
            } else {
                it.remove();
            }
        }
    }

    @Deactivate
    public synchronized void deactivate() {
        auctioneer.unbind(publisher);
        auctioneer.unbind(pmTimeService);
        auctioneer.unbind(executorService);
        marketBasisAdapter.unbind(executorService);
        marketBasisAdapter.unbind(pmTimeService);
        marketBasisAdapter.unbind();

        publisher.close();
    }

    private boolean match(Map<String, Object> properties) {
        return properties.containsKey(AGENT_FILTER_KEY) && properties.get(AGENT_FILTER_KEY)
                                                                     .equals(config.auctioneer_id());
    }

    private ScheduledExecutorService executorService;

    @Reference
    public void setExecutorService(ScheduledExecutorService executorService) {
        this.executorService = executorService;
    }

    private TimeService timeService;
    private net.powermatcher.core.scheduler.service.TimeService pmTimeService;

    private MarketBasisAdapter marketBasisAdapter;

    @Reference
    public void setTimeService(final TimeService timeService) {
        this.timeService = timeService;
        pmTimeService = new net.powermatcher.core.scheduler.service.TimeService() {
            @Override
            public int getRate() {
                throw new UnsupportedOperationException();
            }

            @Override
            public long currentTimeMillis() {
                return timeService.getCurrentTimeMillis();
            }
        };
    }

    @Reference(dynamic = true, multiple = true, optional = true)
    public synchronized void addAgent(AgentService agent, Map<String, Object> properties) {
        if (config == null) {
            // Not configured yet, so just store for now
            agents.add(agent);
        } else if (match(properties)) {
            agents.add(agent);
            if (auctioneer != null) {
                logger.debug("Binding agent [" + agent + "] to auctioneer [" + config.auctioneer_id() + "]");
                auctioneer.bind(agent);
                try {
                    ((AgentConnectorService) agent).bind(auctioneer);
                } catch (Exception e) {
                }
            }
        }
    }

    public synchronized void removeAgent(AgentService agent) {
        if (agents.remove(agent)) {
            logger.debug("Unbinding agent [" + agent + "] from auctioneer [" + config.auctioneer_id() + "]");
            auctioneer.unbind(agent);
            ((AgentConnectorService) agent).unbind(auctioneer);
        }
    }
}
