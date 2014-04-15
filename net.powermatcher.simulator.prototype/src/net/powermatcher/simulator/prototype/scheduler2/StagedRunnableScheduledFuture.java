package net.powermatcher.simulator.prototype.scheduler2;

import java.util.concurrent.RunnableScheduledFuture;

public interface StagedRunnableScheduledFuture<V> extends RunnableScheduledFuture<V> {

	public int getStage();
	
	public long getTime();

}
