package net.powermatcher.agent.telemetry.logging.task;


import java.util.concurrent.ScheduledExecutorService;

import net.powermatcher.core.scheduler.service.SchedulerConnectorService;

/**
 * Marker interface that identifies an (anonymous inner) class as a scheduled task that
 * performs the periodic processing to write captured log records to file.
 * The identification of tasks allows the simulator to control the execution of tasks.   
 * 
 * @author IBM
 * @version 0.9.0
 * 
 *  @see SchedulerConnectorService
 *  @see ScheduledExecutorService
 */
public interface FileUpdateTask extends Runnable {

}
