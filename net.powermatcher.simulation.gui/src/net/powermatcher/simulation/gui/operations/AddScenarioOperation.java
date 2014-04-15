package net.powermatcher.simulation.gui.operations;

import java.util.Date;

import net.powermatcher.simulation.configuration.AsFastAsPossibleSimulationClockDescriptor;
import net.powermatcher.simulation.configuration.MarketBasisDescriptor;
import net.powermatcher.simulation.configuration.ScenarioDescriptor;
import net.powermatcher.simulation.gui.Application;
import net.powermatcher.simulation.gui.ScenarioContainer;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class AddScenarioOperation extends AbstractOperation {
	private ScenarioDescriptor scenarioDescriptor;

	public AddScenarioOperation() {
		super("Undo redo scenario operation");
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		// FIXME this should't be hard coded
		MarketBasisDescriptor marketBasis = new MarketBasisDescriptor();
		marketBasis.setCommodity("electricity");
		marketBasis.setCurrency("EUR");
		marketBasis.setPriceSteps(50);
		marketBasis.setMinimumPrice(0);
		marketBasis.setMaximumPrice(50);

		AsFastAsPossibleSimulationClockDescriptor clock = new AsFastAsPossibleSimulationClockDescriptor();
		clock.setStartTime(new Date(0));
		clock.setTimestepIntervalMillis(30000);

		scenarioDescriptor = new ScenarioDescriptor();
		scenarioDescriptor.setMarketBasisDescriptor(marketBasis);
		scenarioDescriptor.setSimulationClockDescriptor(clock);

		ScenarioContainer scenarios = Application.getInstance().getScenarios();
		scenarios.addChild(scenarioDescriptor);
		scenarios.setActiveScenario(scenarioDescriptor);

		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		Application.getInstance().getScenarios().addChild(scenarioDescriptor);

		if (scenarioDescriptor.isActive()) {
			Application.getInstance().getScenarios().setActiveScenario(scenarioDescriptor);
		}

		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		Application.getInstance().getScenarios().removeChild(scenarioDescriptor);
		return Status.OK_STATUS;
	}

}
