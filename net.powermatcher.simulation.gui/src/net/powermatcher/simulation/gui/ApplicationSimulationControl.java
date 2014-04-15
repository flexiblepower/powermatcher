package net.powermatcher.simulation.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import net.powermatcher.simulation.configuration.ScenarioDescriptor;
import net.powermatcher.simulation.engine.ComponentCreationException;
import net.powermatcher.simulation.engine.ScenarioInitializer;
import net.powermatcher.simulation.engine.SimulationControl;
import net.powermatcher.simulation.engine.SimulationCycleListener;
import net.powermatcher.simulation.engine.dependencyengine.DependencyEngineScenarioInitializer;
import net.powermatcher.simulation.gui.editors.SimulationStateListener;
import net.powermatcher.simulation.gui.views.ConfigurationView;
import net.powermatcher.simulation.logging.Broker;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

public final class ApplicationSimulationControl implements SimulationControl, SimulationCycleListener {
	private long lastSetDelay = 0;
	private final Set<SimulationCycleListener> listeners = new CopyOnWriteArraySet<SimulationCycleListener>();
	private SimulationControl simulation;
	private final List<SimulationStateListener> stateListeners = new ArrayList<SimulationStateListener>();
	TreeViewer viewer;

	@Override
	public void addSimulationCycleListener(SimulationCycleListener listener) {
		this.listeners.add(listener);

		if (this.simulation != null) {
			simulation.addSimulationCycleListener(listener);
		}
	}

	public void addSimulationStateListener(SimulationStateListener listener) {
		this.stateListeners.add(listener);
	}

	@Override
	public long getDelay() {
		if (simulation == null) {
			return this.lastSetDelay;
		}

		return simulation.getDelay();
	}

	@Override
	public long getDelay(TimeUnit unit) {
		if (simulation == null) {
			return unit.convert(this.lastSetDelay, TimeUnit.MILLISECONDS);
		}

		return simulation.getDelay(unit);
	}

	@Override
	public SimulationState getState() {
		if (simulation == null) {
			return SimulationState.STOPPED;
		}

		return simulation.getState();
	}

	@Override
	public synchronized void pause() {
		if (this.simulation == null || this.simulation.getState() != SimulationState.RUNNING) {
			// TODO add message
			return;
		}

		this.simulation.pause();
		// viewer.getControl().setEnabled(true);
	}

	@Override
	public boolean removeSimulationCycleListener(SimulationCycleListener listener) {
		boolean removed = this.listeners.remove(listener);

		if (this.simulation != null) {
			removed |= simulation.removeSimulationCycleListener(listener);
		}

		return removed;
	}

	public boolean removeSimulationStateListener(SimulationStateListener listener) {
		boolean removed = this.stateListeners.remove(listener);
		return removed;
	}

	@Override
	public synchronized void setDelay(long delay, TimeUnit unit) {
		this.lastSetDelay = unit.toMillis(delay);

		if (this.simulation == null) {
			return;
		}

		this.simulation.setDelay(delay, unit);
	}

	@Override
	public void simulationCycleBegins(long timestamp) {
	}

	@Override
	public void simulationCycleFinishes(long timestamp) {
	}

	@Override
	public void simulationFinished() {
		Application.getInstance().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				ApplicationSimulationControl.this.stop();
			}
		});
	}

	@Override
	public void simulationStarts(long timestamp) {
	}

	@Override
	public synchronized void start() {
		if (simulation != null) {
			Shell shell = Application.getInstance().getShell();
			MessageDialog.openInformation(shell, "Info", "A simulation is already running");
		} else if (this.simulation == null || this.simulation.getState() == SimulationState.STOPPED) {
			this.startNew();
		} else if (this.simulation.getState() == SimulationState.PAUSED) {
			this.simulation.start();

		}
	}

	private void startNew() {
		ScenarioInitializer init = new DependencyEngineScenarioInitializer();
		Application application = Application.getInstance();
		ScenarioContainer scenarios = application.getScenarios();
		ScenarioDescriptor activeScenario = scenarios.getActiveScenario();

		if (activeScenario == null) {
			if (scenarios.getChildren().size() == 1) {
				// if there is only one scenario, set it as active
				activeScenario = scenarios.getChildren().get(0);
				scenarios.setActiveScenario(activeScenario);
			} else {
				// TODO show error message
				MessageDialog.openError(Application.getInstance().getShell(), "No scenario Loaded",
						"Please load a scenario file into the application and press start again");
				return;
			}
		}

		try {
			this.simulation = init.initializeScenario(activeScenario, application.getComponentManager());

			this.simulation.addSimulationCycleListener(this);
			for (SimulationCycleListener listener : listeners) {
				this.simulation.addSimulationCycleListener(listener);
			}

			// TODO cast may be dangerous ... change interface or this code ...
			((Broker) init.getBroker()).addDataSink(application.getBroker());
			this.simulation.setDelay(this.lastSetDelay, TimeUnit.MILLISECONDS);
			this.simulation.start();
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			ConfigurationView view = (ConfigurationView) page.findView(ConfigurationView.ID);
			viewer = (TreeViewer) view.getSite().getSelectionProvider();
			viewer.getControl().setEnabled(false);
			for (SimulationStateListener listener : stateListeners) {
				listener.simulationStarted();
			}
		} catch (Exception e) {
			if (this.simulation != null) {
				this.simulation.stop();
				viewer.getControl().setEnabled(true);
				for (SimulationStateListener listener : stateListeners) {
					listener.simulationStopped();
				}
				this.simulation = null;
			}

			if (e instanceof ComponentCreationException) {
				MessageDialog.openError(Application.getInstance().getShell(), "Could not initialize scenario",
						"An error occurred during the initialization of the scenario because a scenario component could not be created: "
								+ ((ComponentCreationException) e).getComponentFactoryId()
								+ "\nThe following error message was provided: " + e.getMessage());

				// TODO Auto-generated catch block
				e.printStackTrace();
			} else {
				MessageDialog.openError(Application.getInstance().getShell(), "Could not initialize scenario",
						"An error occurred during the initialization of the scenario.\nThe following error message was provided: "
								+ e.getMessage());

				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public synchronized void step() {
		if (this.simulation == null) {
			MessageDialog.openInformation(Application.getInstance().getShell(), "Info",
					"You can only step when the simulation is paused");
		}

		this.simulation.step();
	}

	@Override
	public synchronized void stop() {
		if (this.simulation == null) {
			return;
		}

		this.simulation.stop();
		viewer.getControl().setEnabled(true);
		for (SimulationStateListener listener : stateListeners) {
			listener.simulationStopped();
		}
		this.simulation = null;
	}

}