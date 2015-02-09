package net.powermatcher.extensions.connectivity.websockets;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * Servlet which activates the PowerMatcher WebSocket communication.
 * 
 * @author FAN
 * @version 2.0
 */
public class PowermatcherWebSocketServlet extends WebSocketServlet {

    /**
     * SerializerUID
     */
    private static final long serialVersionUID = -8809366066221881974L;

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(WebSocketServletFactory wssf) {
        wssf.register(PowermatcherWebSocket.class);
    }
}
