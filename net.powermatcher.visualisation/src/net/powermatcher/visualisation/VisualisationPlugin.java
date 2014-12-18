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

import net.powermatcher.visualisation.models.LevelModel;
import net.powermatcher.visualisation.models.MenuItemModel;
import net.powermatcher.visualisation.models.NodeModel;
import net.powermatcher.visualisation.models.SubMenuItemModel;

import org.apache.commons.io.IOUtils;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import aQute.bnd.annotation.metatype.Configurable;
import aQute.bnd.annotation.metatype.Meta;

/**
 *  {@link HttpServlet} used by the visualizer frontend.
 * 
 * @author FAN
 * @version 1.0
 */
@Component(
        provide = Servlet.class,
        properties = { "felix.webconsole.title=Powermatcher cluster visualizer",
                "felix.webconsole.label=pm-cluster-visualizer" },
        immediate = true,
        designateFactory = VisualisationPlugin.Config.class)
public class VisualisationPlugin extends HttpServlet {
    private static final long serialVersionUID = -3582669073153236495L;

    /**
     * The slf4j {@link Logger} instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(VisualisationPlugin.class);

    /**
     * The Base path of this servlet.
     */
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

    /**
     * An {@link ConfigurationAdmin} instance
     */
    private ConfigurationAdmin configurationAdmin;

    /**
     * The filter used to alert observables
     */
    private List<String> filter = new ArrayList<>();

    /**
     * The {@link MenuItemModel} items
     */
    private Map<String, MenuItemModel> menuItems;

    /**
     * The method called by OSGi to turn this instance into a managed service.
     */
    @Activate
    public void activate(Map<String, Object> properties) {
        Config config = Configurable.createConfigurable(Config.class, properties);

        menuItems = new HashMap<>();
        fillMenuItems(config.menu());
        LOGGER.info("VisualisationPlugin [{}], activated");
    }

    /**
     * Fills the menuItems Map.
     * 
     * @param input
     *            the List containing String values, separated with ::
     */
    private void fillMenuItems(List<String> input) {
        MenuItemModel menuItem;
        SubMenuItemModel subMenuItem;

        for (String s : input) {
            String[] temp = s.split("::");

            String fpid = temp[1];
            filter.add(fpid);

            menuItem = new MenuItemModel(temp[0]);

            String title = temp[1].substring(fpid.lastIndexOf(".") + 1);
            subMenuItem = new SubMenuItemModel(title, fpid);

            if (menuItems.containsKey(menuItem.getTitle())) {
                menuItems.get(menuItem.getTitle()).addSubMenuItem(subMenuItem);
            } else {
                menuItem.addSubMenuItem(subMenuItem);
                menuItems.put(menuItem.getTitle(), menuItem);
            }
        }
    }

    /**
     * @see HttpServlet#doGet
     */
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

    /**
     * @see HttpServlet#doPost
     */
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

    /**
     * @return the menuItems map as a {@link JsonObject}.
     */
    private JsonObject getMenuAsJson() {

        Gson gson = new Gson();
        JsonObject output = new JsonObject();

        output.add("menu", gson.toJsonTree(menuItems.values()));

        return output;
    }

    /**
     * @return all {@link LevelModel}'s of this cluster as a {@link JsonObject}.
     */
    private JsonObject getNodesAsJson() {

        Gson gson = new GsonBuilder().serializeNulls().create();
        JsonObject output = new JsonObject();

        Map<Integer, LevelModel> levelMap = new HashMap<>();
        Map<String, NodeModel> nodes = new HashMap<>();

        NodeModel node;
        LevelModel levelModel;

        try {
            for (Configuration c : configurationAdmin.listConfigurations(null)) {
                if (filter.contains(c.getFactoryPid())) {

                    String agentId = (String) c.getProperties().get("agentId");
                    String desiredParentId = (String) c.getProperties().get("desiredParentId");
                    String pid = c.getPid();
                    String fpid = c.getFactoryPid();

                    node = new NodeModel(fpid, pid, agentId, desiredParentId);
                    nodes.put(agentId, node);
                }
            }
        } catch (IOException e) {
            LOGGER.equals(e.getMessage());
        } catch (InvalidSyntaxException e) {
            LOGGER.equals(e.getMessage());
        }

        for (NodeModel nm : nodes.values()) {
            int count = 0;
            String desiredParent = nm.getDesiredParentId();

            // determining the level
            while (desiredParent != null && nodes.containsKey(desiredParent)) {
                count++;
                desiredParent = nodes.get(desiredParent).getDesiredParentId();
            }

            if (!levelMap.containsKey(count)) {
                levelModel = new LevelModel(count);
                levelMap.put(count, levelModel);
            }

            levelMap.get(count).addNode(nm);
        }

        output.add("levels", gson.toJsonTree(levelMap.values()));

        return output;
    }

    /**
     * OSGi calls this method to get a resource before calling this.doGet. If this method returns en URL, it returns the
     * resource. If it returns <code>null</code>, it calls this.doGet
     * 
     * @param path
     *            the path of the requested resource
     * @return the {@link URL} if the resource exists or <code>null</code> if it's not an accepted file format.
     */
    protected URL getResource(String path) {
        if (path.endsWith(".js") || path.endsWith(".png") || path.endsWith(".css")) {
            String newPath = path.replaceAll(BASE_PATH + "/", "");
            return getClass().getClassLoader().getResource(newPath);
        } else {
            return null;
        }
    }

    /**
     * This method is used by OSGi to inject the {@link ConfigurationAdmin}.
     * 
     * @param the
     *            {@link ConfigurationAdmin} instance.
     */
    @Reference
    protected void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;
    }
}
