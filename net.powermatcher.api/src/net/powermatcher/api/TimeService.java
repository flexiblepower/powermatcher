package net.powermatcher.api;

import java.util.Date;

/**
 * The TimeService defines the interface used to obtain the current time. The
 * current time could be the actual time, or it could be the simulated time that
 * is advancing at different a rate or stepwise.
 * 
 * @author FAN
 * @version 1.0
 */
public interface TimeService {

	/**
	 * Returns the current time in milliseconds.
	 * 
	 * @return the difference, measured in milliseconds, between the current time and midnight, January 1, 1970 UTC.
	 */
	public long currentTimeMillis();

	/**
	 * Returns the current time in a {@link Date} object.
	 * 
	 * @return A {@link Date} object, representing the current date and time
	 */
	public Date currentDate();
}
