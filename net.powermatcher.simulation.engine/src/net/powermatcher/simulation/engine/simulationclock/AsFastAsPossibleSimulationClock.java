package net.powermatcher.simulation.engine.simulationclock;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * A SimulationClock which lets the simulation run as fast as possible with a
 * constant interval.
 */
public class AsFastAsPossibleSimulationClock implements SimulationClock {

	/** The current timestamp */
	private long currentTimestamp;

	/**
	 * The timestamp at which the simulation is finished. A value of -1
	 * indicates that the simulation doesn't finish by itself.
	 **/
	private final long endTimestamp;

	/** The interval of each cycle */
	private final long period;

	/** The timestamp at which the simulation starts */
	private final long startTime;

	/**
	 * Instantiates a new SimulationClock with an end time
	 * 
	 * @param startTime
	 *            the start time
	 * @param endTime
	 *            the end time
	 * @param period
	 *            the interval between cycles
	 * @param unit
	 *            the TimeUnit of period
	 */
	public AsFastAsPossibleSimulationClock(Date startTime, Date endTime, long period, TimeUnit unit) {
		this(startTime.getTime(), unit.toMillis(period), endTime.getTime());
	}

	/**
	 * Instantiates a new SimulationClock without an end time
	 * 
	 * @param startTime
	 *            the start time
	 * @param period
	 *            the period
	 * @param unit
	 *            the TimeUnit of period
	 */
	public AsFastAsPossibleSimulationClock(Date startTime, long period, TimeUnit unit) {
		this(startTime.getTime(), unit.toMillis(period));
	}

	/**
	 * Instantiates a new SimulationClock without an end time
	 * 
	 * @param startTimeMillis
	 *            the start time in ms
	 * @param periodMillis
	 *            the period between cycles in ms
	 */
	public AsFastAsPossibleSimulationClock(long startTimeMillis, long periodMillis) {
		this(startTimeMillis, periodMillis, -1);
	}

	/**
	 * Instantiates a new SimulationClock with an end time
	 * 
	 * @param startTimeMillis
	 *            the start timestamp in ms
	 * @param periodMillis
	 *            the interval between cycles in ms
	 * @param endTimeMillis
	 *            the end timestamp in ms
	 */
	public AsFastAsPossibleSimulationClock(long startTimeMillis, long periodMillis, long endTimeMillis) {
		this.startTime = startTimeMillis;
		this.period = periodMillis;
		this.currentTimestamp = this.startTime;
		this.endTimestamp = endTimeMillis;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.powermatcher.simulation.engine.simulationclock.SimulationClock#
	 * getNextTimestamp()
	 */
	@Override
	public long getNextTimestamp() {
		this.currentTimestamp += this.period;
		return this.currentTimestamp;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.powermatcher.simulation.engine.simulationclock.SimulationClock#
	 * getStartTimestamp()
	 */
	@Override
	public long getStartTimestamp() {
		return this.startTime;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.powermatcher.simulation.engine.simulationclock.SimulationClock#initialize
	 * ()
	 */
	@Override
	public void initialize() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.powermatcher.simulation.engine.simulationclock.SimulationClock#
	 * simulationIsFinished()
	 */
	@Override
	public boolean simulationIsFinished() {
		if (this.endTimestamp < 0) {
			return false;
		} else {
			return this.currentTimestamp >= this.endTimestamp;
		}
	}
}
