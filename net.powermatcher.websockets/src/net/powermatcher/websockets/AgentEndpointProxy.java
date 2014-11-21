package net.powermatcher.websockets;

import java.io.IOException;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.core.BaseAgent;
import net.powermatcher.websockets.data.MarketBasisModel;
import net.powermatcher.websockets.data.PriceModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

import com.google.gson.Gson;

@Component(designateFactory = AgentEndpointProxy.Config.class, immediate = true, 
	provide = { ObservableAgent.class, AgentEndpoint.class, AgentEndpointProxy.class })
public class AgentEndpointProxy extends BaseAgent implements AgentEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentEndpointProxy.class);

    private String matcherEndpointProxyId;
    
	private org.eclipse.jetty.websocket.api.Session remoteSession;
	
	private Session localSession;
	
	@Meta.OCD
    public static interface Config {
        @Meta.AD(deflt = "concentrator", description = "desired parent to connect to")
        String desiredParentId();

        @Meta.AD(deflt = "agentendpointproxy", description = "local agent identification")
        String agentId();

        @Meta.AD(deflt = "matcherendpointproxy", description = "Remote matcher endpoint proxy")
        String remoteAgentEndpointId();
    }

	@Activate
	public void activate(Map<String, Object> properties) {
        Config config = Configurable.createConfigurable(Config.class, properties);

        this.setDesiredParentId(config.desiredParentId());
        this.setAgentId(config.agentId());
        this.matcherEndpointProxyId = config.remoteAgentEndpointId();
	}
	
	public void remoteAgentConnected(org.eclipse.jetty.websocket.api.Session session) 
			throws OperationNotSupportedException {
		if (this.remoteSession != null && this.remoteSession.isOpen()) {
			throw new OperationNotSupportedException("Remote Agent already connected.");
		}
		
		this.remoteSession = session;
	}
	
	public void remoteAgentDisconnected() {
		this.remoteSession = null;
	}
	
	public void relayBid(Bid newBid) {
		if (this.localSession == null) {
			LOGGER.warn("Desired parent agent not connected, skip sendingg bid update");
			return;
		}

		this.localSession.updateBid(newBid);
	}
	
	public String getMatcherEndpointProxyId() {
		return this.matcherEndpointProxyId;
	}
	
	@Override
	public void connectToMatcher(Session session) {
        this.setClusterId(session.getClusterId());
        this.localSession = session;
	}

	@Override
	public synchronized void updatePrice(Price newPrice) {
		if (this.remoteSession == null || !this.remoteSession.isOpen()) {
			LOGGER.warn("Remote agent not connected, skip sending price update");
			return;
		}		
		
		LOGGER.info("Sending price update to remote agent {}", newPrice);
		try 
		{
			// Create price update message and include marketbasis and clusterid
			PriceModel newPriceModel = new PriceModel();
			newPriceModel.setClusterId(this.getClusterId());
			newPriceModel.setCurrentPrice(newPrice.getCurrentPrice());
			newPriceModel.setMarketBasis(MarketBasisModel.fromMarketBasis(newPrice.getMarketBasis()));
			
			Gson gson = new Gson();
			String message = gson.toJson(newPriceModel, PriceModel.class);
			
			this.remoteSession.getRemote().sendString(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void matcherEndpointDisconnected(Session session) {
		this.localSession = null;
	}
}
