package net.powermatcher.simulation.gui.menus;

import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.gui.operations.AddResourceManagerOperation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.flexiblepower.ral.ResourceManager;

public class AddResourceManagerMenuContribution extends AddNodeMenuContribution {
	public AddResourceManagerMenuContribution() {
	}

	public AddResourceManagerMenuContribution(String id) {
		super(id);
	}

	@Override
	protected boolean filterComponentClass(Class<?> componentClass) {
		return ResourceManager.class.isAssignableFrom(componentClass);
	}

	@Override
	protected void createComponent(String componentFactoryPid, NodeDescriptor parent) {
		try {
			IUndoableOperation operation = new AddResourceManagerOperation(parent, componentFactoryPid, null);

			IWorkbench workbench = PlatformUI.getWorkbench();
			operation.addContext(workbench.getOperationSupport().getUndoContext());
			workbench.getOperationSupport().getOperationHistory().execute(operation, null, null);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
}
