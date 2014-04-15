package net.powermatcher.core.agent.auctioneer.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import junit.framework.Test;
import junit.framework.TestSuite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ AuctioneerResilienceTestAF.class, AuctioneerResilienceAFPriceTests.class, 
	AuctioneerResilienceTestIAQ1.class, AuctioneerResilienceTestIAQ2.class  })
public class AuctioneerTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				AuctioneerTestSuite.class.getName());
		//$JUnit-BEGIN$

		//$JUnit-END$
		return suite;
	}

}
