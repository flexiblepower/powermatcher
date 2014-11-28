package net.powermatcher.visualisation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.powermatcher.api.AgentEndpoint;
import net.powermatcher.api.MatcherEndpoint;
import net.powermatcher.api.Session;
import net.powermatcher.core.sessions.SessionImpl;
import net.powermatcher.core.sessions.SessionManagerInterface;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

@Component(provide = Servlet.class, properties = { "felix.webconsole.title=Powermatcher cluster visualizer",
        "felix.webconsole.label=pm-cluster-visualizer" }, immediate = true)
public class VisualisationPlugin extends HttpServlet {
    private static final long serialVersionUID = 7146852312931261310L;
    private static final Logger LOGGER = LoggerFactory.getLogger(VisualisationPlugin.class);

    private static final String BASE_PATH = "/pm-cluster-visualizer";

    private Map<String, VisualElement> activeElements;

    private Map<String, AgentEndpoint> agentEndpoints;

    /**
     * Holds the matcherEndpoints
     */
    private Map<String, MatcherEndpoint> matcherEndpoints;

    /**
     * Holds the activeSessions
     */
    private Map<String, Session> activeSessions;

    private SessionManagerInterface sessionManager;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String path = req.getPathInfo();

        if (path.equals(BASE_PATH)) {
            resp.sendRedirect(BASE_PATH.substring(1) + "/index.html");
            return;
        }

        // html pages have to be sent here with a Stream because the getResource would only return the html page, not
        // the rest.
        else if (path.endsWith(".html")) {
            resp.setContentType("text/html");

            path = path.substring(path.lastIndexOf("/") + 1);

            InputStream input = getClass().getClassLoader().getResourceAsStream(path);
            if (input == null) {
                LOGGER.debug("Could not find file {}", path);
                resp.sendError(404, "Resource \"" + path + "\" not found.");
            } else {
                LOGGER.debug("Serving file {}", path);
                IOUtils.copy(input, resp.getWriter());
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        if (req.getPathInfo().endsWith("icons.xml")) {
            return;
        }

        String incommingJson = req.getReader().readLine();

        JsonObject jobject = new Gson().fromJson(incommingJson, JsonObject.class);

        String requestKind = jobject.get("requestKind").getAsString();

        if ("saveState".equals(requestKind)) {
            LOGGER.info("Saving state");

        } else if ("loadState".equals(requestKind)) {
            LOGGER.info("Loading state");
            updateData();
            String output = handleLoadState();
            resp.getWriter().write(output);
        }
    }

    private void updateData() {
        this.agentEndpoints = sessionManager.getAgentEndpoints();
        this.matcherEndpoints = sessionManager.getMatcherEndpoints();
        this.activeSessions = sessionManager.getActiveSessions();
        activeElements = new HashMap<>();
    }

    private String handleLoadState() {

        StringBuilder sb = new StringBuilder();

        // Common first object
        sb.append("{\"zoom\":1,\"fileName\":\"\",\"exportPath\":\"\",\"reference\":0,\"min\":0,\"max\":0.99,\"step\":100,\"significance\":2}ARRAYSPLIT[");

        processSessions();
        procssSeparate();

        for (VisualElement v : activeElements.values()) {
            sb.append(v.toString());
            sb.append(",");
        }

        sb.deleteCharAt(sb.lastIndexOf(","));
        sb.append("]");

        // had to be cleared for next time, in case sessions are removed.
        activeElements = new HashMap<>();

        return sb.toString();
    }

    private void procssSeparate() {

        for (String s : agentEndpoints.keySet()) {

            if (!activeElements.containsKey(s)) {
                createAgentElement(agentEndpoints.get(s), s);
            }
        }

        for (String s : matcherEndpoints.keySet()) {
            if (!activeElements.containsKey(s)) {
                createMatcherElement(matcherEndpoints.get(s), s);
            }
        }
    }

    private void processSessions() {

        for (Session s : activeSessions.values()) {

            if (s instanceof SessionImpl) {
                SessionImpl temp = (SessionImpl) s;

                VisualElement agentElement = createAgentElement(temp.getAgentEndpoint(), temp.getAgentId());

                VisualElement matcherElement = createMatcherElement(temp.getMatcherEndpoint(), temp.getMatcherId());
                matcherElement.addChild(agentElement);
            }
        }
    }

    private VisualElement createMatcherElement(MatcherEndpoint matcherEndpoint, String matcherId) {
        Kind kind = null;

        if (matcherEndpoint instanceof AgentEndpoint) {
            kind = Kind.CONCENTRATOR;
        } else {
            kind = Kind.AUCTIONEER;
        }

        return createVisualElement(matcherId, kind);
    }

    private VisualElement createAgentElement(AgentEndpoint agentEndpoint, String agentId) {
        Kind kind = null;

        if (agentEndpoint instanceof MatcherEndpoint) {
            kind = Kind.CONCENTRATOR;
        } else {
            kind = Kind.DEVICEAGENT;
        }

        return createVisualElement(agentId, kind);
    }

    private VisualElement createVisualElement(String id, Kind kind) {
        VisualElement output = null;

        if (activeElements.containsKey(id)) {
            output = activeElements.get(id);
        } else {
            output = new VisualElement(kind, id);
            activeElements.put(output.getName(), output);
        }
        return output;
    }

    protected URL getResource(String path) {

        // OSGi's AbstractWebConsole.doGet calls this method, so
        // don't remove it!
        // if you return null, it'll continue with AbstractWebConsole.doGet and return the resource. If you return null,
        // it calls this.doGet()

        if (path.endsWith(".js") || path.endsWith(".png") || path.endsWith(".css")) {
            String newPath = path.replaceAll(BASE_PATH + "/", "");
            return getClass().getClassLoader().getResource(newPath);
        } else {
            // if you return null, it calls to doGet
            return null;
        }
    }

    @Reference
    public void setSessionManager(SessionManagerInterface sessionManager) {
        this.sessionManager = sessionManager;
    }
}
