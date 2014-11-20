package net.powermatcher.websockets;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.core.BaseAgent;
import net.powermatcher.websockets.data.BidModel;
import net.powermatcher.websockets.data.MarketBasisModel;
import net.powermatcher.websockets.data.PriceModel;

import org.eclipse.jetty.websocket.api.CloseStatus;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

@WebSocket()
@Component(designateFactory = MatcherEndpointProxy.Config.class, immediate = true, provide = { ObservableAgent.class,
    MatcherEndpoint.class })
public class MatcherEndpointProxy extends BaseAgent implements MatcherEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatcherEndpointProxy.class);

	@Meta.OCD
    public static interface Config {
        @Meta.AD(deflt = "")
        String desiredParentId();

        @Meta.AD(deflt = "matcherendpointproxy")
        String agentId();

        @Meta.AD(deflt = "matcherendpointproxy")
        String matcherEndpointProxy();
    }
    
    // TODO make this configurable
    private final String uri = "ws://localhost:8080/powermatcher/websockets/agentendpoint"; 
	
    private Session localSession;
	private org.eclipse.jetty.websocket.api.Session remoteSession;
	private WebSocketClient client;

	private MarketBasis marketBasis;
	private Bid lastBid;
	
	@Activate
	public synchronized void activate(Map<String, Object> properties) {
        Config config = Configurable.createConfigurable(Config.class, properties);

        this.setDesiredParentId(config.desiredParentId());
        this.setAgentId(config.agentId());
        
        connect();
	}

	private synchronized void connect() {
        client = new WebSocketClient();
        URI powermatcherUri;
		try {
			powermatcherUri = new URI(this.uri);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Malformed URL for powermatcher websocket endpoint. Reason {}", e);
			return;
		}

        ClientUpgradeRequest request = new ClientUpgradeRequest();
        try {
        	client.start();
	        Future<org.eclipse.jetty.websocket.api.Session> connectFuture =
	        		client.connect(this, powermatcherUri, request);
	        LOGGER.info("Connecting to : {}", request);

        // Wait TODO configurable for connection time
            org.eclipse.jetty.websocket.api.Session newRemoteSession = 
            		connectFuture.get(60, TimeUnit.SECONDS);

            this.remoteSession = newRemoteSession;
        } catch (Throwable t) {
            // TODO handle errors on failed connections
			LOGGER.error("Unable to connect to remote agent. Reason {}", t);

			this.remoteSession = null;
			
        	return;
        }
		
        
		// TODO Wait for marketbasis response?
		// TODO Synchronization
	}
	
	@Deactivate
	public synchronized void deactivate() {
		// TODO ( ( ClientContainer )container ).stop();?
		if (this.remoteSession != null && this.remoteSession.isOpen()) {
			this.remoteSession.close(new CloseStatus(0, "Normal disconnect"));
		}
		
		if (this.client != null && !this.client.isStopped()) {
			try {
				this.client.stop();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		this.remoteSession = null;		
	}
	
	@Override
	public boolean connectToAgent(Session session) {
        // TODO how to receive remote marketBasis? session.setMarketBasis(marketBasis); -> response of server after connect
		// TODO how to receive remote clusterId? session.setClusterId(this.getClusterId()); -> response of server after connect
		// TODO how to handle local / remote agentId?
		// TODO how to handle local / remote desiredParentId?
		// TODO maybe via querystring during connection?
		// TODO how to handle security (HTTPS and identity)?

		
		// Check status of remote agent connection
		if (this.remoteSession == null || !this.remoteSession.isOpen()) {
	        LOGGER.warn("Remote agent is not connected, unable to connect local agent with session [{}]", session.getSessionId());
			return false;
		}
		
		// Connect to matcher
		session.setClusterId(this.getClusterId());
		session.setMarketBasis(this.marketBasis);
		
        this.localSession = session;
        LOGGER.info("Agent connected with session [{}]", session.getSessionId());
		
		return true;
	}

	@Override
	public void agentEndpointDisconnected(Session session) {
		// TODO is this correct approach?

		if (this.remoteSession != null && this.remoteSession.isOpen()) {
			this.remoteSession.close();
			this.remoteSession = null;
		}

		this.localSession = null;
        LOGGER.info("Agent disconnected with session [{}]", session.getSessionId());
	}

	@Override
	public void updateBid(Session session, Bid newBid) {
		if (this.localSession != session) {
			LOGGER.warn("Received bid update for unknown session.");

			// TODO throw correct exception
			return;
		}
		
		// Relay bid to remote agent
		try {
			// Convert to JSON and send
			BidModel newBidModel = new BidModel();
			newBidModel.setBidNumber(newBid.getBidNumber());
			newBidModel.setDemand(newBid.getDemand());
			newBidModel.convertPricePoints(newBid.getPricePoints());
			newBidModel.setMarketBasis(MarketBasisModel.fromMarketBasis(newBid.getMarketBasis()));
			
			Gson gson = new Gson();
			String message = gson.toJson(newBidModel, BidModel.class);
			this.remoteSession.getRemote().sendString(message);
		} catch (Throwable t) {
			LOGGER.error("Unable to send new bid to remote agent. Reason {}", t);
			
			// TODO catch and throw correct exception
		}		
	}
	
    @OnWebSocketConnect
    public void onConnect(org.eclipse.jetty.websocket.api.Session session) {
        LOGGER.info("Connected: {}", session);
    	
        // TODO handle this?
    }
	
    @OnWebSocketClose
    public void onDisconnect(int statusCode, String reason) {    	
        LOGGER.info("Connection closed: {} - {}", statusCode, reason);

        // TODO handle this?
    }
    
	@OnWebSocketMessage
	// public void onMessage(Price newPrice) {
	public void onMessage(String message) {
		try {
			Gson gson = new Gson();
			PriceModel newPriceModel = gson.fromJson(message, PriceModel.class);
			LOGGER.info("Received price update from remote agent {}", message);
			
			// Relay price update to local agent
			Price newPrice = new Price(MarketBasisModel.fromMarketBasisModel(newPriceModel.getMarketBasis()), 
					newPriceModel.getCurrentPrice());
			
			// TODO move to different location (on handshake)
			if (this.localSession.getMarketBasis() == null) {
				this.localSession.setMarketBasis(newPrice.getMarketBasis());			
			}
			
			this.localSession.updatePrice(newPrice);
		} catch (JsonSyntaxException e) {
			LOGGER.warn("Unable to understand message from remote agent: {}", message);
		}
	}
}