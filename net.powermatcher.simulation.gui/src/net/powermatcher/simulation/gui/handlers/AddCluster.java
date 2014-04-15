package net.powermatcher.simulation.gui.handlers;

import net.powermatcher.simulation.configuration.ScenarioDescriptor;
import net.powermatcher.simulation.gui.operations.AddClusterOperation;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class AddCluster extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		StructuredSelection selection = (StructuredSelection) HandlerUtil.getActiveMenuSelection(event);
		if (selection.getFirstElement() instanceof ScenarioDescriptor == false) {
			return null;
		}

		ScenarioDescriptor scenarioDescriptor = (ScenarioDescriptor) selection.getFirstElement();
		IUndoableOperation operation = new AddClusterOperation(scenarioDescriptor);

		IWorkbench workbench = PlatformUI.getWorkbench();
		operation.addContext(workbench.getOperationSupport().getUndoContext());
		workbench.getOperationSupport().getOperationHistory().execute(operation, null, null);

		return null;
	}
}
