package net.powermatcher.simulation.gui.handlers;

import net.powermatcher.simulation.configuration.DeviceAgentNodeDescriptor;
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

public class RemoveAgent extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		StructuredSelection selection = (StructuredSelection) HandlerUtil.getActiveMenuSelection(event);
		NodeDescriptor<?> node = (NodeDescriptor<?>) selection.getFirstElement();
		if (node == null) {
			return null;
		}

		if (node instanceof DeviceAgentNodeDescriptor) {
			IUndoableOperation operation = new RemoveNodeOperation(node);

			IWorkbench workbench = PlatformUI.getWorkbench();
			operation.addContext(workbench.getOperationSupport().getUndoContext());
			workbench.getOperationSupport().getOperationHistory().execute(operation, null, null);
		} else {
			MessageDialog.openInformation(Application.getInstance().getShell(), "Info",
					"The selection is not a Device Agent node");
		}

		return null;
	}

}
