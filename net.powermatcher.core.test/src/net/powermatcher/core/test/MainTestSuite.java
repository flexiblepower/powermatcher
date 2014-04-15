package net.powermatcher.core.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.powermatcher.core.agent.auctioneer.test.AuctioneerTestSuite;
import net.powermatcher.core.agent.concentrator.test.ConcentratorTestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ AuctioneerTestSuite.class, ConcentratorTestSuite.class, 
	CommFrameworkTestSuite.class, PwmCoreUnitTestSuite.class})
public class MainTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite(MainTestSuite.class.getName());
		//$JUnit-BEGIN$

		//$JUnit-END$
		return suite;
	}
}
