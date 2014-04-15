package net.powermatcher.simulation.gui.menus;

import net.powermatcher.core.agent.framework.Agent;
import net.powermatcher.core.agent.framework.MatcherAgent;
import net.powermatcher.fpai.agent.FPAIAgent;
import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.gui.operations.AddAgentOperation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class AddAgentMenuContribution extends AddNodeMenuContribution {
	public AddAgentMenuContribution() {
	}

	public AddAgentMenuContribution(String id) {
		super(id);
	}

	@Override
	protected boolean filterComponentClass(Class<?> componentClass) {
		return Agent.class.isAssignableFrom(componentClass)
				&& MatcherAgent.class.isAssignableFrom(componentClass) == false
				&& FPAIAgent.class.isAssignableFrom(componentClass) == false;
	}

	@Override
	protected void createComponent(String componentFactoryPid, NodeDescriptor parent) {
		try {
			IUndoableOperation operation = new AddAgentOperation(parent, componentFactoryPid, null);

			IWorkbench workbench = PlatformUI.getWorkbench();
			operation.addContext(workbench.getOperationSupport().getUndoContext());
			workbench.getOperationSupport().getOperationHistory().execute(operation, null, null);
		} catch (ExecutionException e) {
			e.printStackTrace();
		}
	}
}
