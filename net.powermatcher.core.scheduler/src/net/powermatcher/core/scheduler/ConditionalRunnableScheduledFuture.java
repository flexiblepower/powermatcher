package net.powermatcher.core.scheduler;


import java.util.concurrent.RunnableScheduledFuture;

/**
 * @author IBM
 * @version 0.9.0
 */
public interface ConditionalRunnableScheduledFuture<V> extends RunnableScheduledFuture<V> {

	/**
	 * @return
	 */
	public int getCoreIndex();

	/**
	 * @return
	 */
	public int getCycle();

	/**
	 * @return
	 */
	public long getTime();

	/**
	 * @return
	 */
	public boolean isReadyToRun();

	/**
	 * @param readyToRun
	 */
	public void setReadyToRun(boolean readyToRun);

}
