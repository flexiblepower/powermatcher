package net.powermatcher.runtime.sessions.test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import net.powermatcher.api.Session;
import net.powermatcher.api.data.MarketBasis;
import net.powermatcher.mock.MockAgent;
import net.powermatcher.mock.MockContext;
import net.powermatcher.mock.MockMatcherAgent;
import net.powermatcher.runtime.SessionManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * JUnit tests for the {@link SessionManager} class.
 *
 * @author FAN
 * @version 2.0
 */
public class SessionManagerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private static final String AUCTIONEER_NAME = "auctioneer";
    private static final String AGENT_ID = "testAgent";
    private static final String CLUSTER_ID = "testCluster";

    private SessionManager sessionManager;
    private MockMatcherAgent auctioneer;
    private MockAgent testAgent;

    @Before
    public void setUp() {
        auctioneer = new MockMatcherAgent(AUCTIONEER_NAME, CLUSTER_ID);
        auctioneer.setMarketBasis(new MarketBasis("something", "YYY", 10, 0, 1));
        auctioneer.setContext(new MockContext(0));

        sessionManager = new SessionManager();

        testAgent = new MockAgent(AGENT_ID);
        testAgent.setDesiredParentId("auctioneer");
    }

    @After
    public void tearDown() {
        sessionManager.removeAgentEndpoint(testAgent);
        sessionManager.removeMatcherEndpoint(auctioneer);
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
    }

    @Test
    public void testaddMatcherEndpointTwice() {
        sessionManager.addAgentEndpoint(testAgent);
        sessionManager.addMatcherEndpoint(auctioneer);
        sessionManager.addMatcherEndpoint(auctioneer);

        Session agentSession = testAgent.getSession();
        assertEquals(AUCTIONEER_NAME, agentSession.getMatcherId());
    }

    @Test
    public void testremoveAgentEndpoint() {
        sessionManager.addAgentEndpoint(testAgent);
        sessionManager.addMatcherEndpoint(auctioneer);

        Session session = testAgent.getSession();
        assertThat(session, is(notNullValue()));

        sessionManager.removeAgentEndpoint(testAgent);
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
