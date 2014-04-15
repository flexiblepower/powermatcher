package net.powermatcher.simulator.prototype.scheduler2.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import net.powermatcher.simulator.prototype.TimeSource;
import net.powermatcher.simulator.prototype.pmcore.Agent;
import net.powermatcher.simulator.prototype.pmcore.Auctioneer;
import net.powermatcher.simulator.prototype.pmcore.Concentrator;
import net.powermatcher.simulator.prototype.pmcore.DeviceAgent;
import net.powermatcher.simulator.prototype.pmcore.RandomMustRunDeviceAgent;
import net.powermatcher.simulator.prototype.pmcore.VariableDeviceAgent;
import net.powermatcher.simulator.prototype.scheduler2.VirtualScheduledThreadPoolExecutor;
import net.powermatcher.simulator.prototype.viz.PriceVisualizationAgent;

public class VirtualScheduledThreadPoolExecutorTest1 {
	public static void main(String[] args) throws InterruptedException {
		test(10, 1000, 4);
	}

	private static void test(int concentratorCount, int deviceAgentCount, int threadCount) throws InterruptedException {
		int auctioneerStage = 0;
		int concentratorStage = 1;
		int deviceAgentStage = 2;

		final VirtualScheduledThreadPoolExecutor executor = new VirtualScheduledThreadPoolExecutor(threadCount, 0);

		final TimeSource timeSource = new ExecutorAsTimeSource(executor);

		TimeUnit unit = TimeUnit.MINUTES;

		Auctioneer auctioneer = new Auctioneer("AA  ");
		auctioneer.setTimeSource(timeSource);
		auctioneer.addAgent(new PriceVisualizationAgent(timeSource));

		executor.scheduleAtFixedRate(auctioneer, 15, 1, unit, auctioneerStage);

		for (int i = 0; i < concentratorCount; i++) {
			final Concentrator concentrator = new Concentrator("CA " + i);
			concentrator.setTimeSource(timeSource);
			concentrator.setMatcher(auctioneer);
			auctioneer.addAgent(createAsyncAgentLink(executor, concentrator, concentratorStage));

			executor.scheduleAtFixedRate(concentrator, 15, 1, unit, concentratorStage);

			for (int j = 0; j < deviceAgentCount / 2; j++) {
				final DeviceAgent agent = new RandomMustRunDeviceAgent("DAR" + i + "-" + j);
				agent.setTimeSource(timeSource);
				agent.setMatcher(concentrator);
				concentrator.addAgent(createAsyncAgentLink(executor, agent, deviceAgentStage));

				executor.scheduleAtFixedRate(agent, 15, 1, unit, deviceAgentStage);
			}

			for (int j = 0; j < deviceAgentCount / 2; j++) {
				final DeviceAgent agent = new VariableDeviceAgent("DAV" + i + "-" + j);
				agent.setTimeSource(timeSource);
				agent.setMatcher(concentrator);
				concentrator.addAgent(createAsyncAgentLink(executor, agent, deviceAgentStage));

				executor.scheduleAtFixedRate(agent, 15, 1, unit, deviceAgentStage);
			}
		}

		executor.start();
		Thread.sleep(concentratorCount * 60 * 1000);
		executor.shutdown();
	}

	private static Agent createAsyncAgentLink(final VirtualScheduledThreadPoolExecutor executor, final Agent agent,
			final int stage) {
		InvocationHandler handler = new InvocationHandler() {
			public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
				if ("setPrice".equals(method.getName())) {
					executor.submit(new Runnable() {
						public void run() {
							try {
								method.invoke(agent, args);
							} catch (Exception e) {
							}
						}
					}, stage);

					return null;
				} else {
					return method.invoke(agent, args);
				}
			}
		};

		return (Agent) Proxy.newProxyInstance(Agent.class.getClassLoader(), new Class[] { Agent.class }, handler);
	}

	private static final class ExecutorAsTimeSource implements TimeSource {
		private final VirtualScheduledThreadPoolExecutor executor;

		private ExecutorAsTimeSource(VirtualScheduledThreadPoolExecutor executor) {
			this.executor = executor;
		}

		@Override
		public long getCurrentTimeMillis() {
			return executor.simulatedTimeMillis();
		}

		@Override
		public Date getCurrentTime() {
			return executor.simulatedTime();
		}
	}
}
