package net.powermatcher.websockets;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.Bid;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PricePoint;
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
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

@WebSocket()
@Component(designateFactory = MatcherEndpointProxy.Config.class, immediate = true, provide = { ObservableAgent.class,
    MatcherEndpoint.class })
public class MatcherEndpointProxy extends BaseAgent implements MatcherEndpoint {
    private static final Logger LOGGER = LoggerFactory.getLogger(MatcherEndpointProxy.class);

    private static final int RECONNECT_TIMER = 30;
    
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
	
    /**
     * Scheduler that can schedule commands to run after a given delay, or to execute periodically.
     */
    private ScheduledExecutorService scheduler;

    /**
     * A delayed result-bearing action that can be cancelled.
     */
    private ScheduledFuture<?> scheduledFuture;
	
	@Activate
	public synchronized void activate(Map<String, Object> properties) {
		// Read configuration properties
        Config config = Configurable.createConfigurable(Config.class, properties);
        this.setDesiredParentId(config.desiredParentId());
        this.setAgentId(config.agentId());

        // Start connector thread
        scheduledFuture = this.scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                connectRemote();
            }
        }, 0, RECONNECT_TIMER, TimeUnit.SECONDS);
	}
	
	@Deactivate
	public synchronized void deactivate() {
		// Stop connector thread
		this.scheduledFuture.cancel(false);

		// Disconnect the agent
		this.disconnectRemote();
	}

    @Reference
    public void setExecutorService(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }
    
	private synchronized boolean connectRemote() {
		if (!this.isLocalConnected()) {
			// Don't connect when no agentRole is connected
			return true;
		}
	
		if (this.isRemoteConnected()) {
			// Already connected, skip connection
			return true;
		}
		
		// Try to setup a new websocket connection.		
        client = new WebSocketClient();
        URI powermatcherUri;
		try {
			powermatcherUri = new URI(this.uri);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Malformed URL for powermatcher websocket endpoint. Reason {}", e);
			return false;
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
			
        	return false;
        }
		
		// TODO Wait for marketbasis response?
		// TODO Synchronization        
        return true;
	}
	
	private synchronized void disconnectRemote() {
		// Terminate remote session (if any)
		if (this.isRemoteConnected()) {
			this.remoteSession.close(new CloseStatus(0, "Normal disconnect"));
		}

		this.remoteSession = null;		

		// Stop the client
		if (this.client != null && !this.client.isStopped()) {
			try {
				this.client.stop();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private boolean isLocalConnected() {
		return this.localSession != null;
	}

	private boolean isRemoteConnected() {
		return this.remoteSession != null && this.remoteSession.isOpen();
	}
	
	@Override
	public boolean connectToAgent(Session session) {
        // TODO how to receive remote marketBasis? session.setMarketBasis(marketBasis); -> response of server after connect
		// TODO how to receive remote clusterId? session.setClusterId(this.getClusterId()); -> response of server after connect
		// TODO how to handle local / remote agentId?
		// TODO how to handle local / remote desiredParentId?
		// TODO maybe via querystring during connection?
		// TODO how to handle security (HTTPS and identity)?

		
		// TODO always connect to agentRole? Check status of remote agent connection
		/*
		if (this.remoteSession == null || !this.remoteSession.isOpen()) {
	        LOGGER.warn("Remote agent is not connected, unable to connect local agent with session [{}]", session.getSessionId());
			return false;
		}
		*/
		
		// Provide remote matcher information (if available)
		session.setClusterId(this.getClusterId());
		session.setMarketBasis(this.marketBasis);

        this.localSession = session;
        LOGGER.info("Agent connected with session [{}]", session.getSessionId());

        // Initiate a remote connection
		connectRemote();

		return true;
	}

	@Override
	public void agentEndpointDisconnected(Session session) {
		// Disconnect local agent
		this.localSession = null;
        LOGGER.info("Agent disconnected with session [{}]", session.getSessionId());

        // Disconnect remote agent
		this.disconnectRemote();
	}

	@Override
	public void updateBid(Session session, Bid newBid) {
		if (this.localSession != session) {
			LOGGER.warn("Received bid update for unknown session.");

			// TODO throw correct exception
			return;
		}
		
		if (!isRemoteConnected()) {
			LOGGER.warn("Received bid update, but remote agent is not connected.");
			return;
		}
		
		// Relay bid to remote agent
		try {
			// Convert to JSON and send
			BidModel newBidModel = new BidModel();
			newBidModel.setBidNumber(newBid.getBidNumber());
			
			// Caution, include either pricepoints or demand, not both!
			PricePoint[] pricePoints = newBid.getPricePoints();
			if (pricePoints == null || pricePoints.length == 0) {
				newBidModel.setDemand(newBid.getDemand());
			} else  {
				newBidModel.convertPricePoints(pricePoints);
			}
			
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