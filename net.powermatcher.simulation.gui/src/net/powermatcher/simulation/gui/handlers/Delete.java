package net.powermatcher.simulation.gui.handlers;

import java.util.Iterator;

import net.powermatcher.simulation.configuration.ClusterDescriptor;
import net.powermatcher.simulation.configuration.ConcentratorNodeDescriptor;
import net.powermatcher.simulation.configuration.DeviceAgentNodeDescriptor;
import net.powermatcher.simulation.configuration.FPAIDeviceAgentNodeDescriptor;
import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.configuration.ResourceDriverNodeDescriptor;
import net.powermatcher.simulation.configuration.ResourceManagerNodeDescriptor;
import net.powermatcher.simulation.configuration.ScenarioDescriptor;
import net.powermatcher.simulation.gui.Application;
import net.powermatcher.simulation.gui.operations.RemoveClusterOperation;
import net.powermatcher.simulation.gui.operations.RemoveNodeOperation;
import net.powermatcher.simulation.gui.operations.RemoveScenarioOperation;
import net.powermatcher.simulation.gui.views.ConfigurationView;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class Delete extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
		ConfigurationView view = (ConfigurationView) page.findView(ConfigurationView.ID);
		IStructuredSelection selection = (IStructuredSelection) view.getSite().getSelectionProvider().getSelection();

		if (selection == null) {
			MessageDialog.openInformation(Application.getInstance().getShell(), "Delete failed",
					"The selection is not valid");
		}

		Iterator<?> iterator = selection.iterator();
		while (iterator.hasNext()) {
			Object item = iterator.next();

			if (item instanceof ConcentratorNodeDescriptor || item instanceof DeviceAgentNodeDescriptor
					|| item instanceof FPAIDeviceAgentNodeDescriptor || item instanceof ResourceManagerNodeDescriptor
					|| item instanceof ResourceDriverNodeDescriptor) {
				execute(new RemoveNodeOperation((NodeDescriptor<?>) item));
			} else if (item instanceof ClusterDescriptor) {
				execute(new RemoveClusterOperation((ClusterDescriptor) item));
			} else if (item instanceof ScenarioDescriptor) {
				execute(new RemoveScenarioOperation((ScenarioDescriptor) item));
			}
		}

		return null;
	}

	private void execute(IUndoableOperation operation) throws ExecutionException {
		IWorkbench workbench = PlatformUI.getWorkbench();
		operation.addContext(workbench.getOperationSupport().getUndoContext());
		workbench.getOperationSupport().getOperationHistory().execute(operation, null, null);
	}
}
