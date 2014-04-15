package net.powermatcher.simulation.gui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.ui.PlatformUI;

public class Undo extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
			IOperationHistory operationHistory = PlatformUI.getWorkbench().getOperationSupport().getOperationHistory();
			IUndoContext undoContext = PlatformUI.getWorkbench().getOperationSupport().getUndoContext();
			operationHistory.undo(undoContext,null , null);
		return null;
	}

}
