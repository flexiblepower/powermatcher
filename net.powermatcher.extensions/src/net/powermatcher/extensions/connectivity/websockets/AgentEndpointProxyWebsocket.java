package net.powermatcher.extensions.connectivity.websockets;

import java.io.IOException;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.connectivity.AgentEndpointProxy;
import net.powermatcher.api.data.Price;
import net.powermatcher.api.data.PriceUpdate;
import net.powermatcher.api.monitoring.ObservableAgent;
import net.powermatcher.core.connectivity.BaseAgentEndpointProxy;
import net.powermatcher.extensions.connectivity.websockets.json.PmJsonSerializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

@Component(designateFactory = AgentEndpointProxyWebsocket.Config.class, immediate = true, 
	provide = { ObservableAgent.class, AgentEndpoint.class, AgentEndpointProxy.class, AgentEndpointProxyWebsocket.class })
public class AgentEndpointProxyWebsocket extends BaseAgentEndpointProxy {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentEndpointProxyWebsocket.class);
	
	@Meta.OCD
    public static interface Config {
        @Meta.AD(deflt = "concentrator", description = "desired parent to connect to")
        String desiredParentId();

        @Meta.AD(deflt = "agentendpointproxy", description = "local agent identification")
        String agentId();

        @Meta.AD(deflt = "matcherendpointproxy", description = "Remote matcher endpoint proxy")
        String remoteAgentEndpointId();
    }
    
	private org.eclipse.jetty.websocket.api.Session remoteSession;
	
	@Activate
	public void activate(Map<String, Object> properties) {
        Config config = Configurable.createConfigurable(Config.class, properties);

        this.setDesiredParentId(config.desiredParentId());
        this.setAgentId(config.agentId());
        this.setMatcherEndpointProxyId(config.remoteAgentEndpointId());
	}
	
	@Deactivate
	public void deactivated() {
		if (this.isRemoteConnected()) {
			this.remoteSession.close();
		}
	}

	public void remoteAgentConnected(org.eclipse.jetty.websocket.api.Session session) 
			throws OperationNotSupportedException {
		if (this.isRemoteConnected()) {
			throw new OperationNotSupportedException("Remote Agent already connected.");
		}
		
		this.remoteSession = session;
		
		/* TODO
		// Send cluster info
		try 
		{
			// Create price update message
			ClusterInfoModel clusterInfo = new ClusterInfoModel();
			clusterInfo.setClusterId(this.getClusterId());
			// TODO clusterInfo.setMarketBasis(this.getLocalMarketBasis());
			PmMessage pmMessage = new PmMessage();
			pmMessage.setPayload(clusterInfo);
			pmMessage.setPayloadType(PayloadType.CLUSTERINFO);
			
			PmMessageSerializer serializer = new PmMessageSerializer();
			String message = serializer.serializeClusterInfo(clusterInfo);
			this.remoteSession.getRemote().sendString(message);
		} catch (IOException e) {
			LOGGER.warn("Unable to send price update to remote agent, reason {}", e);
		}
		*/
	}
	
	public void remoteAgentDisconnected() {
		this.remoteSession = null;
	}

	@Override
	public boolean isRemoteConnected() {
		return this.remoteSession != null && this.remoteSession.isOpen();
	}

	@Override
	public void updateRemotePrice(PriceUpdate newPrice) {
		try 
		{
			// Create price update message
			PmJsonSerializer serializer = new PmJsonSerializer();
			String message = serializer.serializePriceUpdate(newPrice);
			this.remoteSession.getRemote().sendString(message);
		} catch (IOException e) {
			LOGGER.warn("Unable to send price update to remote agent, reason {}", e);
		}
	}
}
