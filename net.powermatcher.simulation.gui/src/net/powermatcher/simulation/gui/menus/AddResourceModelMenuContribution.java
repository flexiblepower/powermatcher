package net.powermatcher.simulation.gui.menus;

import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.gui.operations.AddResourceModelOperation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.flexiblepower.ral.ResourceDriver;

public class AddResourceModelMenuContribution extends AddNodeMenuContribution {
	public AddResourceModelMenuContribution() {
	}

	public AddResourceModelMenuContribution(String id) {
		super(id);
	}

	@Override
	protected boolean filterComponentClass(Class<?> componentClass) {
		return ResourceDriver.class.isAssignableFrom(componentClass);
	}

	@Override
	protected void createComponent(String componentFactoryPid, NodeDescriptor parent) {
		try {
			IUndoableOperation operation = new AddResourceModelOperation(parent, componentFactoryPid, null);

			IWorkbench workbench = PlatformUI.getWorkbench();
			operation.addContext(workbench.getOperationSupport().getUndoContext());
			workbench.getOperationSupport().getOperationHistory().execute(operation, null, null);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
}
