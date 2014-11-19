package net.powermatcher.visualisation;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Component;

@Component(provide = Servlet.class, properties = { "felix.webconsole.title=Powermatcher cluster visualizer",
        "felix.webconsole.label=pm-cluster-visualizer" })
public class VisualisationPlugin extends HttpServlet {
    private static final long serialVersionUID = 7146852312931261310L;
    private static final Logger LOGGER = LoggerFactory.getLogger(VisualisationPlugin.class);

    private static final String BASE_PATH = "/pm-cluster-visualizer";

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

        LOGGER.info("In POST");

        // PrintWriter w = resp.getWriter();
        //
        // String path = req.getPathInfo();
        // if (path.startsWith("/fpai-connection-manager")) {
        // path = path.substring(24);
        // if (!path.isEmpty() && path.charAt(0) == '/') {
        // path = path.substring(1);
        // }
        // }
        // log.debug("path: " + path);
        // if (path.endsWith(".json")) {
        // resp.setContentType("application/json");
        // }
        // if (path.equals("autoconnect.json")) {
        // log.debug("autoconnect called");
        // connectionManager.autoConnect();
        // w.print("{\"autoconnected\": true, \"class\": \"\"}");
        // } else if (path.equals("connect.json")) {
        // final String id = req.getParameter("id");
        // if (connectionCache.containsKey(id)) {
        // PotentialConnection connection = connectionCache.get(id);
        // if (!connection.isConnected()) {
        // log.debug("Calling connect for " + id);
        // try {
        // connection.connect();
        // w.print("{\"status\": \"Connected " + id + "\", \"class\": \"\"}");
        // } catch (IllegalStateException e) {
        // log.error(e.getMessage());
        // e.printStackTrace();
        // w.print("{\"status\": \"Connect was called for " + id
        // + ", but " + e.getMessage() + "\", \"class\": \"ui-state-error\"}");
        // }
        //
        // } else {
        // log.error("Connect was called for " + id + ", but it was already connected");
        // w.print("{\"status\": \"Connect was called for " + id
        // + ", but it was already connected\", \"class\": \"ui-state-error\"}");
        // }
        // } else {
        // log.error("Connect was called for " + id + ", but it was not found in the cache");
        // w.print("{\"status\": \"Connect was called for " + id
        // + ", but it was not found in the cache\", \"class\": \"ui-state-error\"}");
        // }
        // } else if (path.equals("disconnect.json")) {
        // final String id = req.getParameter("id");
        // if (connectionCache.containsKey(id)) {
        // PotentialConnection connection = connectionCache.get(id);
        // if (connection.isConnected()) {
        // log.debug("Calling disconnect for " + id);
        // connection.disconnect();
        // w.print("{\"status\": \"Disconnected " + id + "\", \"class\": \"\"}");
        // } else {
        // w.print("{\"status\": \"Disconnect was called for " + id
        // + ", but it was already disconnected\", \"class\": \"ui-state-error\"}");
        // log.error("Disconnect was called for " + id + ", but it was already disconnected");
        // }
        // } else {
        // w.print("{\"status\": \"Disonnect was called for " + id
        // + ", but it was not found in the cache\", \"class\": \"ui-state-error\"}");
        // log.error("Disonnect was called for " + id + ", but it was not found in the cache");
        // }
        // } else {
        // w.print("POST Not yet implemented: " + path);
        // }
        // w.close();
    }

    // private void sendJson(HttpServletResponse resp, String graphJson) {
    // LOGGER.debug("Sending nodes and edges as JSON");
    // resp.setContentType("application/json");
    // try {
    // PrintWriter w = resp.getWriter();
    // w.print(graphJson);
    // w.close();
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }

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
