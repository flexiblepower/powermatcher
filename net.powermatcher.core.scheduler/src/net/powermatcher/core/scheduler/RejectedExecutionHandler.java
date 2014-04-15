package net.powermatcher.core.scheduler;

/********************************************
 * Copyright (c) 2012, 2013 Alliander.      *
 * All rights reserved.                     *
 *                                          *
 * Derived from code written by Doug Lea    *
 * with assistance from members of JCP      *
 * JSR-166 Expert Group that was released   *
 * to the public domain, as explained at    *
 * http://creativecommons.org/              *
 * licenses/publicdomain                    *
 *                                          *
 * Contributors:                            *
 *     IBM - derived API and implementation *
 *******************************************/

import java.util.concurrent.RejectedExecutionException;

/**
 * @author IBM
 * @version 0.9.0
 */
public interface RejectedExecutionHandler {

    /**
     * Method that may be invoked by a {@link ThreadPoolExecutor} when
     * {@link ThreadPoolExecutor#execute execute} cannot accept a
     * task.  This may occur when no more threads or queue slots are
     * available because their bounds would be exceeded, or upon
     * shutdown of the Executor.
     *
     * <p>In the absence of other alternatives, the method may throw
     * an unchecked {@link RejectedExecutionException}, which will be
     * propagated to the caller of {@code execute}.
     *
     * @param r the runnable task requested to be executed
     * @param executor the executor attempting to execute this task
     * @throws RejectedExecutionException if there is no remedy
     */
    void rejectedExecution(Runnable r, ThreadPoolExecutor executor);
}
