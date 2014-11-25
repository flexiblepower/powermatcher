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

    private Map<String, VisualElement> activeElements = new HashMap<>();
    
    @Reference
    public void setSessionManager(SessionManagerInterface sessionManager) {
        this.sessionManager = sessionManager;
    }

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

        // TODO For some reason, the frontend does a POST with res/icons.xml. Adding this for now to prevent json parser
        // errors.
        if (req.getPathInfo().endsWith("icons.xml")) {
            return;
        }

        // TODO move load to doGet()?

        String incommingJson = req.getReader().readLine();

        // TODO JsonSyntaxException? Send an errorMessage?
        JsonObject jobject = new Gson().fromJson(incommingJson, JsonObject.class);

        String requestKind = jobject.get("requestKind").getAsString();

        if (requestKind.equals("saveState")) {
            LOGGER.info("Saving state");

        } else if (requestKind.equals("loadState")) {
            LOGGER.info("Loading state");

            String output = handleLoadState();
            resp.getWriter().write(output);
        }
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

        // TODO copy off agentEndpoints and matcherEndpoints? Concurrency?
        for (String s : sessionManager.getAgentEndpoints().keySet()) {

            if (!activeElements.containsKey(s)) {
                createAgentElement(sessionManager.getAgentEndpoints().get(s), s);
            }
        }

        for (String s : sessionManager.getMatcherEndpoints().keySet()) {
            if (!activeElements.containsKey(s)) {
                createMatcherElement(sessionManager.getMatcherEndpoints().get(s), s);
            }
        }
    }

    private void processSessions() {

        for (Session s : sessionManager.getActiveSessions().values()) {

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

    @SuppressWarnings("unused")
    private URL getResource(String path) {

        // I know it's set to private and never used locally. But OSGi's AbstractWebConsole.doGet calls this method, so
        // don't remove it!
        // if you return null, it'll continue with AbstractWebConsole.doGet and return the resource. If you return null,
        // it calls this.doGet()

        if (path.endsWith(".js") || path.endsWith(".png") || path.endsWith(".css")) {
            path = path.replaceAll(BASE_PATH + "/", "");
            URL url = getClass().getClassLoader().getResource(path);
            return url;
        } else {
            // if you return null, it calls to doGet
            return null;
        }
    }
}
