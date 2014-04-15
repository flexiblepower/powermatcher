package net.powermatcher.simulation.gui.menus;

import net.powermatcher.fpai.agent.FPAIAgent;
import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.gui.operations.AddFPAIAgentOperation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class AddFPAIAgentMenuContribution extends AddNodeMenuContribution {
	public AddFPAIAgentMenuContribution() {
	}

	public AddFPAIAgentMenuContribution(String id) {
		super(id);
	}

	@Override
	protected boolean filterComponentClass(Class<?> componentClass) {
		return FPAIAgent.class.isAssignableFrom(componentClass);
	}

	@Override
	protected void createComponent(String componentFactoryPid, NodeDescriptor parent) {
		try {
			IUndoableOperation operation = new AddFPAIAgentOperation(parent, componentFactoryPid, null);

			IWorkbench workbench = PlatformUI.getWorkbench();
			operation.addContext(workbench.getOperationSupport().getUndoContext());
			workbench.getOperationSupport().getOperationHistory().execute(operation, null, null);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
}
