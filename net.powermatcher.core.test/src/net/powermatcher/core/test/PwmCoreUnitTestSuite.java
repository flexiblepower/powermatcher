package net.powermatcher.core.test;

import junit.framework.Test;
import junit.framework.TestSuite;
import net.powermatcher.core.data.test.BidInfoTest;
import net.powermatcher.core.data.test.PriceInfoTest;
import net.powermatcher.core.messaging.framework.test.TopicTest;
import net.powermatcher.core.messaging.protocol.adapter.test.han.test.HANBidMessageTest;
import net.powermatcher.core.messaging.protocol.adapter.test.han.test.HANPriceInfoMessageTest;
import net.powermatcher.core.messaging.protocol.adapter.test.internal.test.InternalBidMessageTest;
import net.powermatcher.core.messaging.protocol.adapter.test.internal.test.InternalPriceInfoMessageTest;
import net.powermatcher.core.messaging.protocol.adapter.test.log.test.BidLogMessageTest;
import net.powermatcher.core.messaging.protocol.adapter.test.log.test.PriceLogMessageTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({  BidInfoTest.class, PriceInfoTest.class, 
	HANBidMessageTest.class, HANPriceInfoMessageTest.class,
	InternalBidMessageTest.class,  InternalPriceInfoMessageTest.class,
	BidLogMessageTest.class, PriceLogMessageTest.class, TopicTest.class, 
	})
public class PwmCoreUnitTestSuite {

	public static Test suite() {
		TestSuite suite = new TestSuite(PwmCoreUnitTestSuite.class.getName());
		//$JUnit-BEGIN$

		//$JUnit-END$
		return suite;
	}
}
