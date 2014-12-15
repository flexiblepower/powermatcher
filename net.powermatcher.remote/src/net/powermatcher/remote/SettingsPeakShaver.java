package net.powermatcher.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

@Component(provide = Servlet.class, immediate = true)
public class SettingsPeakShaver extends HttpServlet {

    private static final long serialVersionUID = 2215458949793062542L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentWhitelist.class);

    private static ConfigurationAdmin configurationAdmin;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String payload = getPayload(req);

        updateBordersPeakShaver(payload, resp);
    }

    private void updateBordersPeakShaver(String payload, HttpServletResponse resp) {
        ConcurrentMap<String, List<String>> settingsConcentrators = new ConcurrentHashMap<String, List<String>>();
        List<String> propsConcentrator = new ArrayList<String>();

        try {
            for (Configuration c : configurationAdmin.listConfigurations(null)) {
                Dictionary<String, Object> properties = c.getProperties();

                if (c.getFactoryPid().equals("net.powermatcher.core.concentrator.PeakShavingConcentrator")) {
                    payload = payload.replace("[", "").replace("]", "");
                    payload = payload.replace("{", "").replace("}", "");
                    String[] newAgentsLst = payload.split(",");

                    String floor = newAgentsLst[0].replace("\"", "");
                    properties.put("floor", floor);

                    String ceiling = newAgentsLst[1].replace("\"", "");
                    properties.put("ceiling", ceiling);

                    c.update(properties);

                    LOGGER.info("PeakShaver updated with floor: " + properties.get("floor") + " and ceiling: " + properties.get("ceiling"));
                    propsConcentrator.add(floor);
                    propsConcentrator.add(ceiling);

                    String agentId = (String) properties.get("agentid");
                    settingsConcentrators.put(agentId, propsConcentrator);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidSyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Gson gson = new Gson();
        try {
            resp.getWriter().print(gson.toJson(settingsConcentrators));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private String getPayload(HttpServletRequest req) {
        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = req.getReader();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return buffer.toString();
    }

    @Reference
    protected void setConfigurationAdmin(ConfigurationAdmin configurationAdmin) {
        this.configurationAdmin = configurationAdmin;
    }
}
