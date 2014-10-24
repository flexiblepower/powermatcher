package net.powermatcher.core.scheduler.service;



/**
 * The TimeService defines the interface obtain the current time.
 * The current time could be the actual time, or it could be the simulated time
 * that is advancing at different rate or stepwise.
 *  
 * @author IBM
 * @version 0.9.0
 */
public interface TimeServicable {

	/**
	 * Get the current or simulated time expressed as milliseconds since
	 * the time 00:00:00 UTC on January 1, 1970.
	 * @return Current or simulated milllisecond time.
	 * @see System#currentTimeMillis()
	 */
	public long currentTimeMillis();

	/**
	 * Get the rate at which time progresses.
	 * The normal rate is 1, a rate of 0 means time is progressing stepwise.
	 * @return The rate at which time progresses, or 0 if progressing stepwise.
	 */
	public int getRate();

}
