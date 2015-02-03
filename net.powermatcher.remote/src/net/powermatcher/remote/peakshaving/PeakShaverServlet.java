package net.powermatcher.remote.peakshaving;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.powermatcher.core.concentrator.PeakShavingConcentrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * The PeakShavingServlet makes it possible to remotely update the measurements of the {@link PeakShavingConcentrator}s
 * in the system. This is done by sending a list of agentIds and measurements in JSON format like this:
 *
 * <pre>
 * [{
 *   "agentId": "ps1",
 *   "measurement": 4874.3
 * },{
 *   "agentId": "ps2",
 *   "measurement": 92334.5
 * }]
 * </pre>
 *
 * @author FAN
 * @version 1.0
 */
@Component(provide = Servlet.class, properties = "alias=/peakshaver")
public class PeakShaverServlet
    extends HttpServlet {

    private static final String KEY_AGENT_ID = "agentId";

    private static final long serialVersionUID = 2215458949793062542L;

    private static final Logger LOGGER = LoggerFactory.getLogger(PeakShaverServlet.class);

    private final Map<String, PeakShavingConcentrator> concentrators = new ConcurrentHashMap<String, PeakShavingConcentrator>();

    @Reference(dynamic = true, multiple = true)
    public void addConcentrator(PeakShavingConcentrator concentrator, Map<String, Object> properties) {
        Object agentId = properties.get(KEY_AGENT_ID);
        if (agentId == null || concentrators.containsKey(agentId.toString())) {
            LOGGER.warn("Illegal configuration for PeakShavingConcentrator: agentId already in use or not available");
        } else {
            concentrators.put(agentId.toString(), concentrator);
        }
    }

    public void removeConcentrator(PeakShavingConcentrator concentrator, Map<String, Object> properties) {
        Object agentId = properties.get(KEY_AGENT_ID);
        if (concentrators.get(agentId) == concentrator) {
            concentrators.remove(agentId);
        }
    }

    /**
     * Set remote new borders (floor and ceiling) for {@link PeakShavingConcentrator}
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        GsonBuilder gb = new GsonBuilder();
        Type listType = new TypeToken<List<Measurement>>() {
        }.getType();
        List<Measurement> measurements = gb.create().fromJson(new InputStreamReader(req.getInputStream()), listType);

        PrintWriter w = resp.getWriter();

        LOGGER.debug("Received measurements: " + measurements);

        for (Measurement measurement : measurements) {
            PeakShavingConcentrator concentrator = concentrators.get(measurement.getAgentId());
            if (concentrator != null) {
                concentrator.setMeasuredFlow(measurement.getMeasurement());
                w.println("Changed measurement of agent [" + measurement.getAgentId()
                          + "] to "
                          + measurement.getMeasurement());
            } else {
                w.println("No agent with id [" + measurement.getAgentId() + "] is available");
                LOGGER.info("No agent with id [{}] is available", measurement.getAgentId());
            }
        }
    }
}
