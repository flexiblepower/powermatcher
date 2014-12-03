package net.powermatcher.visualisation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

@Component(
        provide = Servlet.class,
        properties = { "felix.webconsole.title=Powermatcher cluster visualizer",
                "felix.webconsole.label=pm-cluster-visualizer" },
        immediate = true,
        designateFactory = VisualisationPlugin.Config.class)
public class VisualisationPlugin extends HttpServlet {
    private static final long serialVersionUID = -3582669073153236495L;

    private static final Logger LOGGER = LoggerFactory.getLogger(VisualisationPlugin.class);

    private static final String BASE_PATH = "/pm-cluster-visualizer";

    /**
     * OSGI configuration of the {@link SimpleObserver}
     */
    public static interface Config {
        @Meta.AD(
                required = true,
                deflt = "Auctioneer::net.powermatcher.core.auctioneer.Auctioneer, Concentrator::net.powermatcher.core.concentrator.Concentrator,"
                        + "DeviceAgent::net.powermatcher.examples.Freezer, DeviceAgent::net.powermatcher.examples.PVPanelAgent",
                description = "A list of all the OSGi Menu items that have to be used. It's menu::submenu")
        List<String> menu();
    }

    private ConfigurationAdmin configurationAdmin;

    private List<String> filter = new ArrayList<>();

    private Map<String, List<String>> menu = new HashMap<>();

    @Activate
    public void activate(Map<String, Object> properties) {
        Config config = Configurable.createConfigurable(Config.class, properties);

        List<String> tempList;

        for (String s : config.menu()) {
            String[] temp = s.split("::");
            filter.add(temp[1]);

            if (!menu.containsKey(temp[0])) {
                tempList = new ArrayList<>();
                tempList.add(temp[1]);

                menu.put(temp[0], tempList);
            } else {
                tempList = menu.get(temp[0]);
                tempList.add(temp[1]);

                menu.put(temp[0], tempList);
            }
        }

        LOGGER.info("VisualisationPlugin [{}], activated");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String path = req.getPathInfo();

        if (path.equals(BASE_PATH)) {
            resp.sendRedirect(BASE_PATH.substring(1) + "/index.html");
            return;
        }

        // html pages have to be sent here with a Stream because the getResource would only return the html page, not
        // the rest.
        if (path.endsWith(".html")) {
            String newPath = path.replaceAll(BASE_PATH + "/", "");
            resp.setContentType("text/html");
            InputStream input = getClass().getClassLoader().getResourceAsStream(newPath);
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

        String requestType = req.getParameter("requestType");

        JsonObject output = null;
        if ("nodes".equals(requestType)) {

            LOGGER.info("Returning the Nodes");
            resp.setContentType("application/json");
            output = getNodesAsJson();
        }

        if ("menu".endsWith(requestType)) {
            LOGGER.info("Returning the menu");
            resp.setContentType("application/json");
            output = getMenuAsJson();
        }

        resp.getWriter().print(output.toString());
    }

    private JsonObject getMenuAsJson() {

        JsonObject output = new JsonObject();

        JsonArray jarray = new JsonArray();

        JsonObject tempObject;
        JsonObject tempInnerObject;
        JsonArray tempArray;

        for (String s : menu.keySet()) {

            tempObject = new JsonObject();
            tempArray = new JsonArray();

            tempObject.addProperty("title", s);

            for (String str : menu.get(s)) {
                tempInnerObject = new JsonObject();
                tempInnerObject.addProperty("title", str.substring(str.lastIndexOf(".") + 1));
                tempInnerObject.addProperty("fpid", str);
                tempArray.add(tempInnerObject);
            }

            tempObject.add("items", tempArray);

            jarray.add(tempObject);
        }

        output.add("menu", jarray);

        return output;
    }

    private JsonObject getNodesAsJson() {
        JsonObject output = new JsonObject();

        JsonArray agents = new JsonArray();
        JsonArray connections = new JsonArray();

        JsonObject agent;
        JsonObject connection;

        // TODO no configurations means nullpointer for some reason
        // you need this to work when you start with a black slate
        try {
            for (Configuration c : configurationAdmin.listConfigurations(null)) {
                if (filter.contains(c.getFactoryPid())) {
                    agent = new JsonObject();
                    connection = new JsonObject();

                    String fpid = c.getFactoryPid();
                    String agentId = (String) c.getProperties().get("agentId");

                    agent.addProperty("fpid", fpid);
                    agent.addProperty("pid", c.getPid());
                    agent.addProperty("agentId", agentId);

                    agents.add(agent);

                    String desiredParentId = (String) c.getProperties().get("desiredParentId");

                    connection.addProperty("matcherRole", desiredParentId);
                    connection.addProperty("agentRole", agentId);

                    connections.add(connection);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidSyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        output.add("agents", agents);
        output.add("connections", connections);

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
    protected void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;
    }
}
