package net.powermatcher.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.powermatcher.api.Agent;
import net.powermatcher.api.WhiteList;
import net.powermatcher.core.concentrator.Concentrator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

import com.google.gson.Gson;

@Component(provide = Servlet.class, immediate = true)
public class AgentWhitelist extends HttpServlet {

    private static final long serialVersionUID = -595238687774948434L;

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentWhitelist.class);

    /**
     * Holds the whiteLists
     */
    private static Map<String, WhiteList> whiteLists = new HashMap<String, WhiteList>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ConcurrentMap<String, List<String>> whiteListAgents = null;
        String path = req.getPathInfo();
        if (path != null) {
            // call for concentratorId
            String agentId = getAgentParam(path);
            whiteListAgents = getConcentratorWhiteList(agentId);
        } else {
            // call for all concentrators
            whiteListAgents = getAllAgentsWhiteList();
        }

        returnGson(whiteListAgents, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ConcurrentMap<String, List<String>> whiteListAgents = null;
        String path = req.getPathInfo();
        String payload = getPayload(req);
        if (path != null) {
            // call for concentratorId
            String agentId = getAgentParam(path);
            whiteListAgents = createConcentratorWhiteList(getPayloadAgents(payload), agentId);
        } else {
            // call for all concentrators
            whiteListAgents = createWhiteList(getPayloadAgents(payload));
        }
        returnGson(whiteListAgents, resp);
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ConcurrentMap<String, List<String>> whiteListAgents = null;
        String path = req.getPathInfo();
        String payload = getPayload(req);

        if (path != null) {
            // call for concentratorId
            String agentId = getAgentParam(path);
            whiteListAgents = addConcentratorWhiteList(getPayloadAgents(payload), agentId);
        } else {
            // call for all concentrators
            whiteListAgents = addWhiteList(getPayloadAgents(payload));
        }

        returnGson(whiteListAgents, resp);
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ConcurrentMap<String, List<String>> whiteListAgents = null;
        String path = req.getPathInfo();
        String payload = getPayload(req);

        if (path != null) {
            // call for concentrator
            String agentId = getAgentParam(path);
            whiteListAgents = deleteConcentratorWhiteList(getPayloadAgents(payload), agentId);
        } else {
            // call for all concentrators
            whiteListAgents = deleteWhiteList(getPayloadAgents(payload));
        }

        returnGson(whiteListAgents, resp);
    }

    private ConcurrentMap<String, List<String>> getAllAgentsWhiteList() {
        ConcurrentMap<String, List<String>> validAgents = new ConcurrentHashMap<String, List<String>>();
        for (Map.Entry<String, WhiteList> agent : whiteLists.entrySet()) {
            Concentrator whiteListAgent = (Concentrator) agent.getValue();
            validAgents.put(agent.getKey(), whiteListAgent.getWhiteList());
        }
        return validAgents;
    }

    private ConcurrentMap<String, List<String>> getConcentratorWhiteList(String agentId) {
        ConcurrentMap<String, List<String>> validAgents = new ConcurrentHashMap<String, List<String>>();
        for (Map.Entry<String, WhiteList> agent : whiteLists.entrySet()) {
            Concentrator whiteListAgent = (Concentrator) agent.getValue();
            if (agentId.equals(agent.getKey())) {
                validAgents.put(agent.getKey(), whiteListAgent.getWhiteList());
            }
        }
        return validAgents;
    }

    private ConcurrentMap<String, List<String>> createWhiteList(List<String> whiteListAgents) {
        ConcurrentMap<String, List<String>> validAgents = new ConcurrentHashMap<String, List<String>>();
        for (Map.Entry<String, WhiteList> agent : whiteLists.entrySet()) {
            Concentrator whiteListAgent = (Concentrator) agent.getValue();
            validAgents.put(agent.getKey(), whiteListAgent.createWhiteList(whiteListAgents));
        }
        return validAgents;
    }

    private ConcurrentMap<String, List<String>> createConcentratorWhiteList(List<String> whiteListAgents, String agentId) {
        ConcurrentMap<String, List<String>> validAgents = new ConcurrentHashMap<String, List<String>>();
        for (Map.Entry<String, WhiteList> agent : whiteLists.entrySet()) {
            Concentrator whiteListAgent = (Concentrator) agent.getValue();

            if (agentId.equals(whiteListAgent.getAgentId())) {
                validAgents.put(agent.getKey(), whiteListAgent.createWhiteList(whiteListAgents));
            }
        }
        return validAgents;
    }

    private ConcurrentMap<String, List<String>> addWhiteList(List<String> whiteListAgents) {
        ConcurrentMap<String, List<String>> validAgents = new ConcurrentHashMap<String, List<String>>();
        for (Map.Entry<String, WhiteList> agent : whiteLists.entrySet()) {
            Concentrator whiteListAgent = (Concentrator) agent.getValue();
            validAgents.put(agent.getKey(), whiteListAgent.addWhiteList(whiteListAgents));
        }
        return validAgents;
    }

    private ConcurrentMap<String, List<String>> addConcentratorWhiteList(List<String> whiteListAgents, String agentId) {
        ConcurrentMap<String, List<String>> validAgents = new ConcurrentHashMap<String, List<String>>();
        for (Map.Entry<String, WhiteList> agent : whiteLists.entrySet()) {
            Concentrator whiteListAgent = (Concentrator) agent.getValue();

            if (agentId.equals(whiteListAgent.getAgentId())) {
                validAgents.put(agent.getKey(), whiteListAgent.addWhiteList(whiteListAgents));
            }
        }

        return validAgents;
    }

    private ConcurrentMap<String, List<String>> deleteConcentratorWhiteList(List<String> whiteListAgents, String agentId) {
        ConcurrentMap<String, List<String>> validAgents = new ConcurrentHashMap<String, List<String>>();
        for (Map.Entry<String, WhiteList> agent : whiteLists.entrySet()) {
            Concentrator whiteListAgent = (Concentrator) agent.getValue();

            if (agentId.equals(whiteListAgent.getAgentId())) {
                validAgents.put(agent.getKey(), whiteListAgent.removeWhiteList(whiteListAgents));
            }
        }
        return validAgents;
    }

    private ConcurrentMap<String, List<String>> deleteWhiteList(List<String> whiteListAgents) {
        ConcurrentMap<String, List<String>> validAgents = new ConcurrentHashMap<String, List<String>>();
        for (Map.Entry<String, WhiteList> agent : whiteLists.entrySet()) {
            Concentrator whiteListAgent = (Concentrator) agent.getValue();

            validAgents.put(agent.getKey(), whiteListAgent.removeWhiteList(whiteListAgents));
        }
        return validAgents;
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

    private void returnGson(ConcurrentMap<String, List<String>> whiteListAgents, HttpServletResponse resp) {
        Gson gson = new Gson();
        try {
            resp.getWriter().print(gson.toJson(whiteListAgents));
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    private String getAgentParam(String path) {
        Pattern p = Pattern.compile(".*/\\s*(.*)");
        Matcher m = p.matcher(path);
        String patternAgent;
        if (m.find()) {
            patternAgent = (String) m.group().subSequence(1, m.group().length());
            return patternAgent;
        }
        return null;
    }

    private List<String> getPayloadAgents(String payload) {
        payload = payload.replace("[", "").replace("]", "");
        payload = payload.replace("{", "").replace("}", "");

        String[] agentsLst = payload.split(",");
        List<String> whiteListAgents = new ArrayList<String>();
        for (String agent : agentsLst) {
            agent = agent.replace("\"", "");
            agent.trim();
            whiteListAgents.add(agent);
        }

        return whiteListAgents;
    }

    @Reference(dynamic = true, multiple = true, optional = true)
    public void addWhiteList(WhiteList whiteList) {
        Agent agent = (Agent) whiteList;
        String agentId = agent.getAgentId();

        if (agentId == null) {
            LOGGER.warn("WhiteList with agentId is null", whiteList);
        } else {
            whiteLists.put(agentId, whiteList);
        }
    }

    public void removeWhiteList(WhiteList whiteList) {
        Agent agent = (Agent) whiteList;
        String agentId = agent.getAgentId();

        if (agentId != null && whiteLists.get(agentId) == whiteList) {
            whiteLists.remove(agentId);
            LOGGER.info("Removed whiteList: {}", agentId);
        }
    }
}
