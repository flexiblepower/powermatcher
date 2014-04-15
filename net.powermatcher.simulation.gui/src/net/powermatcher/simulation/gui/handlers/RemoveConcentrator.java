package net.powermatcher.simulation.gui.handlers;

import net.powermatcher.simulation.configuration.ConcentratorNodeDescriptor;
import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.gui.Application;
import net.powermatcher.simulation.gui.operations.RemoveNodeOperation;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class RemoveConcentrator extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		StructuredSelection selection = (StructuredSelection) HandlerUtil.getActiveMenuSelection(event);
		if (selection == null || selection.isEmpty()) {
			return null;
		}

		NodeDescriptor<?> node = (NodeDescriptor<?>) selection.getFirstElement();

		if (node instanceof ConcentratorNodeDescriptor) {
			IUndoableOperation operation = new RemoveNodeOperation(node);

			IWorkbench workbench = PlatformUI.getWorkbench();
			operation.addContext(workbench.getOperationSupport().getUndoContext());
			workbench.getOperationSupport().getOperationHistory().execute(operation, null, null);
		} else {
			MessageDialog.openInformation(Application.getInstance().getShell(), "Remove failed",
					"The selection is not a Concentrator node");
		}

		return null;
	}
}
