package net.powermatcher.simulation.gui.menus;

import net.powermatcher.core.agent.concentrator.Concentrator;
import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.gui.operations.AddConcentratorOperation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class AddConcentratorMenuContribution extends AddNodeMenuContribution {
	public AddConcentratorMenuContribution() {
	}

	public AddConcentratorMenuContribution(String id) {
		super(id);
	}

	@Override
	protected boolean filterComponentClass(Class<?> componentClass) {
		return Concentrator.class.isAssignableFrom(componentClass);
	}

	@Override
	protected void createComponent(String componentFactoryPid, NodeDescriptor parent) {
		try {
			IUndoableOperation operation = new AddConcentratorOperation(parent, componentFactoryPid, null);

			IWorkbench workbench = PlatformUI.getWorkbench();
			operation.addContext(workbench.getOperationSupport().getUndoContext());
			workbench.getOperationSupport().getOperationHistory().execute(operation, null, null);
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
