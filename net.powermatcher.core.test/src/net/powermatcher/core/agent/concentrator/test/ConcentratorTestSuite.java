package net.powermatcher.core.agent.concentrator.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import junit.framework.Test;
import junit.framework.TestSuite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ ConcentratorResilienceTestICF.class, ConcentratorResilienceTestICQ.class  })
public class ConcentratorTestSuite {
	public static Test suite() {
		TestSuite suite = new TestSuite(ConcentratorTestSuite.class.getName());
		// $JUnit-BEGIN$

		// $JUnit-END$
		return suite;
	}
}
