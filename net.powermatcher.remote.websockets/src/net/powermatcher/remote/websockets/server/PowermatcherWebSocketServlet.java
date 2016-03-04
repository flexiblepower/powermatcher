package net.powermatcher.remote.websockets.server;

import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.osgi.framework.BundleContext;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

/**
 * Servlet which activates the PowerMatcher WebSocket communication.
 *
 * @author FAN
 * @version 2.1
 */
@Component(designateFactory = PowermatcherWebSocketServlet.Config.class, provide = Servlet.class)
public class PowermatcherWebSocketServlet
    extends WebSocketServlet
    implements WebSocketCreator {
    private static final long serialVersionUID = -8809366066221881974L;

    @Meta.OCD
    public static interface Config {
        @Meta.AD(deflt = "concentrator",
                 description = "The agent identifier of the parent matcher to which "
                               + "agent proxies should be connected ")
        String desiredParentId();

        @Meta.AD(deflt = "/powermatcher/websocket",
                 description = "The path of the URL on which this servlet can be reached")
        String alias();
    }

    private String desiredParentId;
    private BundleContext bundleContext;

    @Activate
    public void activate(BundleContext bundleContext, Map<String, Object> properties) {
        this.bundleContext = bundleContext;
        Config config = Configurable.createConfigurable(Config.class, properties);
        desiredParentId = config.desiredParentId();
    }

    @Override
    public void init() throws ServletException {
        // Hack to make sure that the WebsocketServerFactory is loaded with the correct ClassLoader
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(WebSocketServletFactory.class.getClassLoader());
            super.init();
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(WebSocketServletFactory wssf) {
        wssf.setCreator(this);
    }

    @Override
    public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
        return new AgentEndpointProxy(bundleContext, desiredParentId);
    }
}
