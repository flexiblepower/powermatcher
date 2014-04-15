package net.powermatcher.simulation.gui.handlers;

import net.powermatcher.simulation.gui.operations.AddScenarioOperation;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class AddScenario extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IUndoableOperation addOperation = new AddScenarioOperation();
		
		IWorkbench workbench = PlatformUI.getWorkbench();
		addOperation.addContext(workbench.getOperationSupport().getUndoContext());
		workbench.getOperationSupport().getOperationHistory().execute(addOperation, null, null);
		
		return null;
	}
}
