package net.powermatcher.core.sessions.test;

import static org.junit.Assert.*;
import static org.hamcrest.core.Is.*;
import static org.hamcrest.core.IsNull.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.junit.Before;
import org.junit.Test;

import net.powermatcher.api.Session;
import net.powermatcher.core.auctioneer.Auctioneer;
import net.powermatcher.core.sessions.SessionManager;
import net.powermatcher.core.time.SystemTimeService;
import net.powermatcher.mock.MockAgent;

public class SessionManagerTest {

    private static final String AUCTIONEER_NAME = "auctioneer";
    private static final String CLUSTER_ID = "testCluster";
    private static final String AGENT_ID = "testAgent";

    private SessionManager sessionManager;
    private Auctioneer auctioneer;
    private MockAgent testAgent;

    @Before
    public void setUp() {
        this.auctioneer = new Auctioneer();
        Map<String, Object> auctioneerProperties = new HashMap<String, Object>();
        auctioneerProperties.put("agentId", AUCTIONEER_NAME);
        auctioneerProperties.put("clusterId", CLUSTER_ID);
        auctioneerProperties.put("matcherId", AUCTIONEER_NAME);
        auctioneerProperties.put("commodity", "electricity");
        auctioneerProperties.put("currency", "EUR");
        auctioneerProperties.put("priceSteps", "11");
        auctioneerProperties.put("minimumPrice", "0");
        auctioneerProperties.put("maximumPrice", "10");
        auctioneerProperties.put("bidTimeout", "600");
        auctioneerProperties.put("priceUpdateRate", "1");

        auctioneer.setExecutorService(new ScheduledThreadPoolExecutor(10));
        auctioneer.setTimeService(new SystemTimeService());
        auctioneer.activate(auctioneerProperties);
        this.sessionManager = new SessionManager();
        sessionManager.activate();

        testAgent = new MockAgent(AGENT_ID);
        testAgent.setDesiredParentId("auctioneer");
    }

    @Test
    public void testaddAgentEndpoint() {
        // test matcherless agent
        sessionManager.addAgentEndpoint(testAgent);
        Session agentSession = testAgent.getSession();
        assertNull(agentSession);

        // test agent belonging to session.
        sessionManager.addMatcherEndpoint(auctioneer);
        agentSession = testAgent.getSession();
        assertEquals(AGENT_ID, agentSession.getAgentId());

        // test if session is the same after adding a new agent
        int hashCode = agentSession.hashCode();

        MockAgent agent2 = new MockAgent(AGENT_ID);
        agent2.setDesiredParentId(AUCTIONEER_NAME);

        sessionManager.addAgentEndpoint(agent2);
        int newCode = testAgent.getSession().hashCode();
        assertThat("Codes should be equal", hashCode == newCode, is(true));
    }

    @Test
    public void testaddMatcherEndpoint() {
        sessionManager.addAgentEndpoint(testAgent);
        sessionManager.addMatcherEndpoint(auctioneer);

        Session agentSession = testAgent.getSession();
        assertEquals(AUCTIONEER_NAME, agentSession.getMatcherId());
        assertEquals(CLUSTER_ID, agentSession.getClusterId());
    }

    @Test
    public void testremoveAgentEndpoint() {
        sessionManager.addAgentEndpoint(testAgent);
        sessionManager.addMatcherEndpoint(auctioneer);

        Session session = testAgent.getSession();
        assertThat(session, is(notNullValue()));

        sessionManager.removeAgentEndpoint(testAgent);
        System.out.println(testAgent.getSession().getMatcherId());
        assertThat(testAgent.getSession(), is(nullValue()));
    }

    @Test
    public void testremoveMatcherEndpoint() {
        sessionManager.addAgentEndpoint(testAgent);
        sessionManager.addMatcherEndpoint(auctioneer);

        Session session = testAgent.getSession();
        assertThat(session, is(notNullValue()));

        sessionManager.removeMatcherEndpoint(auctioneer);
        assertThat(testAgent.getSession(), is(nullValue()));

        // re-add matcher, session should be recreated
        sessionManager.addMatcherEndpoint(auctioneer);
        assertThat(session, is(notNullValue()));
    }
}
