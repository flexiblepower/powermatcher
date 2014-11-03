package net.powermatcher.api;

import java.util.Date;

/**
 * The TimeService defines the interface obtain the current time.
 * The current time could be the actual time, or it could be the simulated time
 * that is advancing at different rate or stepwise.
 *  
 * @author IBM
 * @version 0.9.0
 */
public interface TimeService {

	/**
	 * Get the current or simulated time expressed as milliseconds since
	 * the time 00:00:00 UTC on January 1, 1970.
	 * @return Current or simulated milllisecond time.
	 * @see System#currentTimeMillis()
	 */
	public long currentTimeMillis();

	public Date currentDate();
}
