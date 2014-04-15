package net.powermatcher.simulation.gui.operations;

import net.powermatcher.simulation.configuration.ScenarioDescriptor;
import net.powermatcher.simulation.gui.Application;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class RemoveScenarioOperation extends AbstractOperation {
	private ScenarioDescriptor scenarioDescriptor;

	public RemoveScenarioOperation(ScenarioDescriptor scenarioDescriptor) {
		super("Undo Redo Scenario Operation");
		this.scenarioDescriptor = scenarioDescriptor;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		Application.getInstance().getScenarios().removeChild(scenarioDescriptor);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		Application.getInstance().getScenarios().addChild(scenarioDescriptor);

		if (scenarioDescriptor.isActive()) {
			Application.getInstance().getScenarios().setActiveScenario(scenarioDescriptor);
		}

		return Status.OK_STATUS;
	}

}