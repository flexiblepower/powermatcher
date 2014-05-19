package net.powermatcher.simulation.engine.dependencyengine;

import java.io.File;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import net.powermatcher.simulation.configuration.AsFastAsPossibleSimulationClockDescriptor;
import net.powermatcher.simulation.configuration.ClusterDescriptor;
import net.powermatcher.simulation.configuration.DataSinkDescriptor;
import net.powermatcher.simulation.configuration.RealTimeSimulationClockDescriptor;
import net.powermatcher.simulation.configuration.ScenarioDescriptor;
import net.powermatcher.simulation.configuration.SingleFileCsvDataSinkDescriptor;
import net.powermatcher.simulation.engine.ComponentCreationException;
import net.powermatcher.simulation.engine.ComponentManager;
import net.powermatcher.simulation.engine.ScenarioInitializer;
import net.powermatcher.simulation.engine.SimulationControl;
import net.powermatcher.simulation.engine.simulationclock.AsFastAsPossibleSimulationClock;
import net.powermatcher.simulation.engine.simulationclock.RealtimeSimulationClock;
import net.powermatcher.simulation.engine.simulationclock.SimulationClock;
import net.powermatcher.simulation.logging.Broker;
import net.powermatcher.simulation.logging.sink.CsvDataSink;

import org.flexiblepower.time.TimeService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

// FIXME there is no cleanup when a simulation is finished at all!
public class DependencyEngineScenarioInitializer implements ScenarioInitializer {
	private Broker broker;
	private ServiceRegistration fpaiTimeServiceRegistration;

	@Override
	public Broker getBroker() {
		return this.broker;
	}

	@Override
	public SimulationControl initializeScenario(ScenarioDescriptor scenario, ComponentManager componentManager)
			throws ComponentCreationException {
		// Get simulation clock
		SimulationClock simulationClock = getSimulationClock(scenario);

		// Get broker
		broker = new Broker();
		for (DataSinkDescriptor sinkDescriptor : scenario.getDataSinks()) {
			if (sinkDescriptor instanceof SingleFileCsvDataSinkDescriptor) {
				CsvDataSink sink = new CsvDataSink(sinkDescriptor.getDataDescriptors(), new File(
						((SingleFileCsvDataSinkDescriptor) sinkDescriptor).getOutputFile()));
				broker.addDataSink(sink);
			} else {
				throw new UnsupportedOperationException("DataSinkDescriptor \"" + sinkDescriptor.getClass().toString()
						+ "\" not supported");
			}
		}

		DependencyEngine engine = new DependencyEngine(simulationClock);
		engine.addSimulationCycleListener(broker);

		registerEngineAsFPAITimeService(engine, componentManager);

		// Initialize clusters
		ActivityLinkFactory factory = new ActivityLinkFactory(engine, scenario.getMarketBasisDescriptor(), broker,
				componentManager);

		try {
			for (ClusterDescriptor cluster : scenario.getChildren()) {
				factory.generateLinkedActivities(cluster);
			}
		} catch (ComponentCreationException e) {
			broker.simulationFinished();
			broker = null;
			engine.stop();
			throw e;
		}

		// And done!
		return engine;
	}

	private void registerEngineAsFPAITimeService(final DependencyEngine engine, ComponentManager componentManager) {
		BundleContext bundleContext = componentManager.getBundleContext();
		TimeService timeService = new FPAITimeServiceToEngineBridge(engine);

		fpaiTimeServiceRegistration = bundleContext.registerService(TimeService.class.getName(), timeService, null);
	}

	private SimulationClock getSimulationClock(ScenarioDescriptor scenario) {
		SimulationClock simulationClock = null;
		if (scenario.getSimulationClockDescriptor() instanceof RealTimeSimulationClockDescriptor) {
			simulationClock = new RealtimeSimulationClock();
		} else if (scenario.getSimulationClockDescriptor() instanceof AsFastAsPossibleSimulationClockDescriptor) {
			AsFastAsPossibleSimulationClockDescriptor timeDescriptor = (AsFastAsPossibleSimulationClockDescriptor) scenario
					.getSimulationClockDescriptor();
			if (timeDescriptor.getEndTime() == null) {
				simulationClock = new AsFastAsPossibleSimulationClock(timeDescriptor.getStartTime(),
						timeDescriptor.getTimestepIntervalMillis(), TimeUnit.MILLISECONDS);
			} else {
				simulationClock = new AsFastAsPossibleSimulationClock(timeDescriptor.getStartTime(),
						timeDescriptor.getEndTime(), timeDescriptor.getTimestepIntervalMillis(), TimeUnit.MILLISECONDS);
			}
		} else {
			throw new UnsupportedOperationException("SimulationClockDescriptor \""
					+ scenario.getSimulationClockDescriptor().getClass().toString() + "\" not supported");
		}
		return simulationClock;
	}

	private final class FPAITimeServiceToEngineBridge implements TimeService {
		private final DependencyEngine engine;

		private FPAITimeServiceToEngineBridge(DependencyEngine engine) {
			this.engine = engine;
		}

//		@Override
//		public Date getTimeOver(double value, org.flexiblepower.rai.unit.TimeUnit unit) {
//			return new Date((long) (getCurrentTimeMillis() + unit.convertTo(value,
//					org.flexiblepower.rai.unit.TimeUnit.MILLISECONDS)));
//		}
//
//		@Override
//		public Date getTimeOver(Duration duration) {
//			return new Date(getCurrentTimeMillis() + duration.getMilliseconds());
//		}

		@Override
		public Date getTime() {
			return new Date(getCurrentTimeMillis());
		}

		@Override
		public long getCurrentTimeMillis() {
			return engine.currentTimeMillis();
		}
	}

}
