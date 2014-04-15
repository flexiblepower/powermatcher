package net.powermatcher.simulator.prototype.scheduler.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.TimeUnit;

import net.powermatcher.simulator.prototype.pmcore.Agent;
import net.powermatcher.simulator.prototype.pmcore.Auctioneer;
import net.powermatcher.simulator.prototype.pmcore.Concentrator;
import net.powermatcher.simulator.prototype.pmcore.DeviceAgent;
import net.powermatcher.simulator.prototype.pmcore.RandomMustRunDeviceAgent;
import net.powermatcher.simulator.prototype.pmcore.VariableDeviceAgent;
import net.powermatcher.simulator.prototype.scheduler.Scheduler;

public class SchedulerTestBenchmark {
	public static void main(String[] args) throws InterruptedException {
		for (int coreCount = 1; coreCount <= 4; coreCount++) {
			// test(coreCount, 10, 1000);
			test(coreCount, 10, 5000);
		}
	}

	private static void test(int coreCount, int concentratorCount, int agentCount) throws InterruptedException {
		System.out.print(coreCount + "\t" + concentratorCount + "\t" + agentCount + "\t");
		long cycleCount = test(coreCount, concentratorCount, agentCount, TimeUnit.SECONDS.toMillis(10));
		System.out.println(cycleCount);
	}

	private static long test(int threadCount, int concentratorCount, int deviceAgentCount, long runTime)
			throws InterruptedException {
		final Scheduler scheduler = new Scheduler(threadCount);
		TimeUnit unit = TimeUnit.MINUTES;

		Auctioneer auctioneer = new Auctioneer("AA  ");
		auctioneer.setTimeSource(scheduler);

		scheduler.scheduleAtFixedRate(auctioneer, 15, 3, unit, 0);

		for (int i = 0; i < concentratorCount; i++) {
			final Concentrator concentrator = new Concentrator("CA " + i);
			concentrator.setTimeSource(scheduler);
			concentrator.setMatcher(auctioneer);
			auctioneer.addAgent(createAsyncAgentLink(scheduler, concentrator));

			scheduler.scheduleAtFixedRate(concentrator, 15, 2, unit, 1);

			for (int j = 0; j < deviceAgentCount / 2; j++) {
				final DeviceAgent agent = new RandomMustRunDeviceAgent("DAR" + i + "-" + j);
				agent.setTimeSource(scheduler);
				agent.setMatcher(concentrator);
				concentrator.addAgent(createAsyncAgentLink(scheduler, agent));

				scheduler.scheduleAtFixedRate(agent, 15, 1, unit, 2);
			}

			for (int j = 0; j < deviceAgentCount / 2; j++) {
				final DeviceAgent agent = new VariableDeviceAgent("DAV" + i + "-" + j);
				agent.setTimeSource(scheduler);
				agent.setMatcher(concentrator);
				concentrator.addAgent(createAsyncAgentLink(scheduler, agent));

				scheduler.scheduleAtFixedRate(agent, 15, 1, unit, 2);
			}
		}

		scheduler.start();
		Thread.sleep(runTime);
		scheduler.interrupt();
		
		return scheduler.getCycleCount();
	}

	private static Agent createAsyncAgentLink(final Scheduler scheduler, final Agent agent) {
		InvocationHandler handler = new InvocationHandler() {
			public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
				if ("setPrice".equals(method.getName())) {
					scheduler.submit(new Runnable() {
						public void run() {
							try {
								method.invoke(agent, args);
							} catch (Exception e) {
							}
						}
					});

					return null;
				} else {
					return method.invoke(agent, args);
				}
			}
		};

		return (Agent) Proxy.newProxyInstance(Agent.class.getClassLoader(), new Class[] { Agent.class }, handler);
	}
}
