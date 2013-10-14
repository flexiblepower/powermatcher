package net.powermatcher.fpai.test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.Assert;
import junit.framework.TestCase;

public class MockScheduledExecutorTest extends TestCase {
    private MockTimeService timeService;
    private MockScheduledExecutor executor;

    @Override
    public void setUp() throws Exception {
        timeService = new MockTimeService();
        executor = new MockScheduledExecutor(timeService.getFlexiblePowerTimeService());
    }

    @Override
    public void tearDown() throws Exception {
        executor.shutdown();
    }

    public void testExecute() throws InterruptedException, ExecutionException {
        NamedRunnable command = new NamedRunnable("command");
        Assert.assertEquals(0, command.getExecutionCount());

        executor.execute(command);
        Assert.assertEquals(0, command.getExecutionCount());

        executor.executePending();
        Assert.assertEquals(1, command.getExecutionCount());

        executor.executePending();
        Assert.assertEquals(1, command.getExecutionCount());
    }

    public void testSubmitCommand() throws InterruptedException, ExecutionException {
        NamedCallable command = new NamedCallable("command");
        Assert.assertEquals(0, command.getExecutionCount());

        Future<String> future = executor.submit(command);
        Assert.assertEquals(0, command.getExecutionCount());

        executor.executePending();
        Assert.assertEquals(1, command.getExecutionCount());
        Assert.assertEquals("command", future.get());

        executor.executePending();
        Assert.assertEquals(1, command.getExecutionCount());
    }

    public void testScheduleRunnableWithDelay() throws InterruptedException, ExecutionException {
        NamedRunnable command = new NamedRunnable("command");
        Assert.assertEquals(0, command.getExecutionCount());

        ScheduledFuture<?> scheduledFuture = executor.schedule(command, 1, TimeUnit.MINUTES);
        Assert.assertEquals(0, command.getExecutionCount());

        executor.executePending();
        Assert.assertEquals(0, command.getExecutionCount());

        timeService.stepInTime(1, TimeUnit.MINUTES);
        executor.executePending();
        Assert.assertEquals(1, command.getExecutionCount());
        Assert.assertNull(scheduledFuture.get());

        executor.executePending();
        Assert.assertEquals(1, command.getExecutionCount());
    }

    public void testScheduleCallableWithDelay() throws InterruptedException, ExecutionException {
        NamedCallable command = new NamedCallable("command");
        Assert.assertEquals(0, command.getExecutionCount());

        ScheduledFuture<?> scheduledFuture = executor.schedule(command, 1, TimeUnit.MINUTES);
        Assert.assertEquals(0, command.getExecutionCount());

        executor.executePending();
        Assert.assertEquals(0, command.getExecutionCount());

        timeService.stepInTime(1, TimeUnit.MINUTES);
        executor.executePending();
        Assert.assertEquals(1, command.getExecutionCount());
        Assert.assertEquals("command", scheduledFuture.get());

        executor.executePending();
        Assert.assertEquals(1, command.getExecutionCount());

    }

    public void testScheduleAtFixedRate() throws InterruptedException, ExecutionException {
        NamedRunnable command = new NamedRunnable("command");
        Assert.assertEquals(0, command.getExecutionCount());

        ScheduledFuture<?> scheduledFuture = executor.scheduleAtFixedRate(command, 1, 1, TimeUnit.MINUTES);
        Assert.assertEquals(0, command.getExecutionCount());

        executor.executePending();
        Assert.assertEquals(0, command.getExecutionCount());

        timeService.stepInTime(1, TimeUnit.MINUTES);
        executor.executePending();
        Assert.assertEquals(1, command.getExecutionCount());

        executor.executePending();
        Assert.assertEquals(1, command.getExecutionCount());

        timeService.stepInTime(1, TimeUnit.MINUTES);
        executor.executePending();
        Assert.assertEquals(2, command.getExecutionCount());

        executor.executePending();
        Assert.assertEquals(2, command.getExecutionCount());

        scheduledFuture.cancel(true);
        timeService.stepInTime(1, TimeUnit.MINUTES);
        executor.executePending();
        Assert.assertEquals(2, command.getExecutionCount());
    }

    public void testScheduleWithFixedDelay() throws InterruptedException, ExecutionException {
        NamedRunnable command = new NamedRunnable("command");
        Assert.assertEquals(0, command.getExecutionCount());

        ScheduledFuture<?> scheduledFuture = executor.scheduleWithFixedDelay(command, 1, 1, TimeUnit.MINUTES);
        Assert.assertEquals(0, command.getExecutionCount());

        executor.executePending();
        Assert.assertEquals(0, command.getExecutionCount());

        timeService.stepInTime(1, TimeUnit.MINUTES);
        executor.executePending();
        Assert.assertEquals(1, command.getExecutionCount());

        executor.executePending();
        Assert.assertEquals(1, command.getExecutionCount());

        timeService.stepInTime(1, TimeUnit.MINUTES);
        executor.executePending();
        Assert.assertEquals(2, command.getExecutionCount());

        executor.executePending();
        Assert.assertEquals(2, command.getExecutionCount());

        scheduledFuture.cancel(true);
        timeService.stepInTime(1, TimeUnit.MINUTES);
        executor.executePending();
        Assert.assertEquals(2, command.getExecutionCount());
    }

    private static class CountedExecution {
        private final AtomicLong counter = new AtomicLong();

        protected void incExecutionCount() {
            counter.incrementAndGet();
        }

        public long getExecutionCount() {
            return counter.get();
        }
    }

    private static class NamedRunnable extends CountedExecution implements Runnable {
        private final String name;

        public NamedRunnable(String name) {
            this.name = name;
        }

        @Override
        public void run() {
            incExecutionCount();
            System.out.println("- executing: " + name);
        }
    }

    private static class NamedCallable extends CountedExecution implements Callable<String> {
        private final String name;

        public NamedCallable(String name) {
            this.name = name;
        }

        @Override
        public String call() {
            incExecutionCount();
            System.out.println("- executing: " + name);
            return name;
        }
    }
}
