package net.powermatcher.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.powermatcher.core.concentrator.PeakShavingConcentrator;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

import com.google.gson.Gson;

/**
 * 
 * @author FAN
 * @version 2.0
 */
@Component(provide = Servlet.class, immediate = true)
public class SettingsPeakShaver extends HttpServlet {

    private static final long serialVersionUID = 2215458949793062542L;

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsPeakShaver.class);

    /**
     * OSGI ConfigurationAdmin, stores bundle configuration data persistently.
     */
    private static ConfigurationAdmin configurationAdmin;

    /**
     * Set remote new borders (floor and ceiling) for {@link PeakShavingConcentrator}
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        updateBordersPeakShaver(req, resp);
    }

    private void updateBordersPeakShaver(HttpServletRequest req, HttpServletResponse resp) {
        ConcurrentMap<String, List<String>> settingsConcentrators = new ConcurrentHashMap<String, List<String>>();

        try {
            for (Configuration c : configurationAdmin.listConfigurations(null)) {
                Dictionary<String, Object> properties = c.getProperties();

                if (c.getFactoryPid().equals("net.powermatcher.core.concentrator.PeakShavingConcentrator")) {

                    List<String> borderPeakShaver = getFloorAndCeiling(getPayload(req));
                    properties.put("floor", borderPeakShaver.get(0));
                    properties.put("ceiling", borderPeakShaver.get(1));
                    c.update(properties);

                    settingsConcentrators.put((String) properties.get("agentid"), borderPeakShaver);

                    LOGGER.info("PeakShaver updated with floor: " + properties.get("floor") + " and ceiling: "
                            + properties.get("ceiling"));

                    Gson gson = new Gson();
                    resp.getWriter().print(gson.toJson(settingsConcentrators));
                }
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        } catch (InvalidSyntaxException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private List<String> getFloorAndCeiling(String payload) {
        String floor = null;
        String ceiling = null;
        List<String> bordersPeakshaver = new ArrayList<String>();

        payload = payload.replace("[", "").replace("]", "");
        payload = payload.replace("{", "").replace("}", "");
        String[] newAgentsLst = payload.split(",");

        String strFloor = newAgentsLst[0].replace("\"", "");
        String strCeiling = newAgentsLst[1].replace("\"", "");

        final Pattern pattern = Pattern.compile("-?\\d+");
        final Matcher matcherFloor = pattern.matcher(strFloor);

        while (matcherFloor.find()) {
            floor = matcherFloor.group();
            bordersPeakshaver.add(floor);
        }

        final Matcher matcherCeiling = pattern.matcher(strCeiling);
        while (matcherCeiling.find()) {
            ceiling = matcherCeiling.group();
            bordersPeakshaver.add(ceiling);
        }

        return bordersPeakshaver;
    }

    private String getPayload(HttpServletRequest req) {
        StringBuilder buffer = new StringBuilder();
        String line;
        try (BufferedReader reader = req.getReader()) {
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }

        return buffer.toString();
    }

    @SuppressWarnings("static-access")
    @Reference
    protected void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;
    }
}
