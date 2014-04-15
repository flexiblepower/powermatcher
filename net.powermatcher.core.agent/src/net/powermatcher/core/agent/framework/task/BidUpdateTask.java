package net.powermatcher.core.agent.framework.task;


import java.util.concurrent.ScheduledExecutorService;

import net.powermatcher.core.scheduler.service.SchedulerConnectorService;

/**
 * Marker interface that identifies an (anonymous inner) class as a scheduled task that
 * performs the periodic processing to generate and publish an updated bid.
 * The identification of tasks allows the simulator to control the execution of tasks.   
 * 
 * @author IBM
 * @version 0.9.0
 * 
 *  @see SchedulerConnectorService
 *  @see ScheduledExecutorService
 */
public interface BidUpdateTask extends Runnable {

}
