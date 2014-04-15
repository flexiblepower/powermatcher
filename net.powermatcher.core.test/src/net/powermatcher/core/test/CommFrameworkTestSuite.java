package net.powermatcher.core.test;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ SendReceivePriceTestCPF1.class, SendReceivePriceTestCPQ1.class, 
	SendReceiveBidTestCBF1.class, SendReceiveBidTestCBQ1.class })
public class CommFrameworkTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite(CommFrameworkTestSuite.class.getName());
		//$JUnit-BEGIN$

		//$JUnit-END$
		return suite;
	}
}
