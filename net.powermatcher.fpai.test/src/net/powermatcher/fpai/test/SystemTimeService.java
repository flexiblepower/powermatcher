package net.powermatcher.fpai.test;

import net.powermatcher.core.scheduler.service.TimeService;

/** Implementation of the TimeService which uses the system clock */
public class SystemTimeService implements TimeService {
	@Override
	public long currentTimeMillis() {
		// use the system clock
		return System.currentTimeMillis();
	}

	@Override
	public int getRate() {
		// rate is 1:1 to the progress of the system clock
		return 1;
	}
}
