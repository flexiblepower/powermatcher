package net.powermatcher.mock;

import java.util.Date;

import net.powermatcher.api.TimeService;

public class MockTimeService implements TimeService {

	private long now;

	public MockTimeService(Date initialDate) {
		now = initialDate.getTime();
	}

	public MockTimeService(long initialDate) {
		now = initialDate;
	}

	@Override
	public long currentTimeMillis() {
		return now;
	}

	@Override
	public Date currentDate() {
		return new Date(now);
	}

	public void jump(long ms) {
		now += ms;
	}

}
