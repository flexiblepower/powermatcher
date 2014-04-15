package net.powermatcher.agent.peakshavingconcentrator.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.scheduler.SchedulerAdapterFactory;
import net.powermatcher.core.scheduler.service.TimeService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class PeakShavingConcentratorImplUnitTest {
	private final double[] bid = new double[] { 15, 13, 11, 9, 7, 5, 3, 1 };
	private final double[] clipped = new double[] { 13, 13, 11, 9, 7, 5, 3, 3 };
	private final MarketBasis marketBasis = new MarketBasis("electricity", "EUR", 8, 0, 7, 1, 0);

	private ScheduledExecutorService scheduler;
	private TimeService timeSource;
	private MockPeakShavingConcentratorImpl concentrator;

	@Before
	public void setUp() throws Exception {
		SchedulerAdapterFactory schedulerAdapterFactory = SchedulerAdapterFactory.getSchedulerAdapterFactory(); 
		scheduler = schedulerAdapterFactory.getScheduler();
		timeSource = schedulerAdapterFactory.getTimeSource();
		this.concentrator = new MockPeakShavingConcentratorImpl();

		Properties properties = new Properties();
		properties.setProperty("id", "a");
		this.concentrator.setConfiguration(new BaseConfiguration(properties));

		this.concentrator.updateMarketBasis(this.marketBasis);

		this.concentrator.bind(timeSource);
		this.concentrator.bind(scheduler);
	}

	@After
	public void tearDown() throws Exception {
		this.concentrator.unbind(scheduler);
		this.concentrator.unbind(timeSource);
	}

	@Test
	public void testUnconstraint() {
		// submit a bid check propagated bid
		this.concentrator.updateBidInfo("1", new BidInfo(this.marketBasis, this.bid));
		assertArrayEquals(this.bid, this.concentrator.getPropagatedBid().getDemand(), 0);

		// submit a price and check propagated price
		this.concentrator.updatePriceInfo(new PriceInfo(this.marketBasis, 0));
		assertEquals(0, this.concentrator.getPropagatedPrice().getCurrentPrice(), 0);

		// check allocation and flow reduction
		assertEquals(15, this.concentrator.getAllocation(), 0);
		assertEquals(0, this.concentrator.getFlowReduction(), 0);
	}

	@Test
	public void testConstrained() {
		this.concentrator.updateBidInfo("1", new BidInfo(this.marketBasis, this.bid));
		this.concentrator.updatePriceInfo(new PriceInfo(this.marketBasis, 0));

		// set flow constraints and check propagated bid and price
		this.concentrator.setFlowConstraints(14, 2);
		assertArrayEquals(this.clipped, this.concentrator.getPropagatedBid().getDemand(), 0);
		assertEquals(1, this.concentrator.getPropagatedPrice().getCurrentPrice(), 0);

		// check allocation and flow reduction
		assertEquals(13, this.concentrator.getAllocation(), 0);
		assertEquals(2, this.concentrator.getFlowReduction(), 0);
	}

	@Test
	public void testConstrainedWithMeasuredFlows() {
		this.concentrator.updateBidInfo("1", new BidInfo(this.marketBasis, this.bid));
		this.concentrator.updatePriceInfo(new PriceInfo(this.marketBasis, 0));
		this.concentrator.setFlowConstraints(14, 2);

		// set a measured flow and check propagated bid, price and the
		// allocation, uncontrolled flow and flow reduction
		this.concentrator.setMeasuredFlow(16);
		assertEquals(2, this.concentrator.getPropagatedPrice().getCurrentPrice(), 0);
		assertEquals(11, this.concentrator.getAllocation(), 0);
		assertEquals(5, this.concentrator.getUncontrolledFlow(), 0);
		assertEquals(4, this.concentrator.getFlowReduction(), 0);

		// again, with more load
		this.concentrator.setMeasuredFlow(19);
		assertEquals(5, this.concentrator.getPropagatedPrice().getCurrentPrice(), 0);
		assertEquals(5, this.concentrator.getAllocation(), 0);
		assertEquals(14, this.concentrator.getUncontrolledFlow(), 0);
		assertEquals(10, this.concentrator.getFlowReduction(), 0);

		// again, with less load
		this.concentrator.setMeasuredFlow(5);
		assertEquals(1, this.concentrator.getPropagatedPrice().getCurrentPrice(), 0);
		assertEquals(13, this.concentrator.getAllocation(), 0);
		assertEquals(-8, this.concentrator.getUncontrolledFlow(), 0);
		assertEquals(2, this.concentrator.getFlowReduction(), 0);

		// again, with generation
		this.concentrator.setMeasuredFlow(10);
		assertEquals(0, this.concentrator.getPropagatedPrice().getCurrentPrice(), 0);
		assertEquals(15, this.concentrator.getAllocation(), 0);
		assertEquals(-5, this.concentrator.getUncontrolledFlow(), 0);
		assertEquals(0, this.concentrator.getFlowReduction(), 0);

		// again, with load creating an overload
		this.concentrator.setMeasuredFlow(40);
		this.concentrator.setMeasuredFlow(40);
		assertEquals(7, this.concentrator.getPropagatedPrice().getCurrentPrice(), 0);
		assertEquals(1, this.concentrator.getAllocation(), 0);
		assertEquals(39, this.concentrator.getUncontrolledFlow(), 0);
		assertEquals(14, this.concentrator.getFlowReduction(), 0);
	}
}
