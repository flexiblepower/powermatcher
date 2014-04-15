package net.powermatcher.simulator.prototype;

import java.util.Date;

public interface TimeSource {
	long getCurrentTimeMillis();

	Date getCurrentTime();
}
