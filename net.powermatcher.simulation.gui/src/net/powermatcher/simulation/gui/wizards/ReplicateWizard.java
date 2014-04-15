package net.powermatcher.simulation.gui.wizards;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.powermatcher.simulation.configuration.ConcentratorNodeDescriptor;
import net.powermatcher.simulation.configuration.ConfigurationElement;
import net.powermatcher.simulation.configuration.DeviceAgentNodeDescriptor;
import net.powermatcher.simulation.configuration.FPAIDeviceAgentNodeDescriptor;
import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.configuration.ResourceDriverNodeDescriptor;
import net.powermatcher.simulation.configuration.ResourceManagerNodeDescriptor;
import net.powermatcher.simulation.gui.operations.AddAgentOperation;
import net.powermatcher.simulation.gui.operations.AddConcentratorOperation;
import net.powermatcher.simulation.gui.operations.AddFPAIAgentOperation;
import net.powermatcher.simulation.gui.operations.AddNodeOperation;
import net.powermatcher.simulation.gui.operations.AddResourceManagerOperation;
import net.powermatcher.simulation.gui.operations.AddResourceModelOperation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

public class ReplicateWizard extends Wizard implements INewWizard {
	private ReplicateWizardPageOne page;
	private List<NodeDescriptor<?>> selection;

	public ReplicateWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = new ArrayList<NodeDescriptor<?>>();
		for (Object o : selection.toArray()) {
			if (o instanceof NodeDescriptor<?>) {
				this.selection.add((NodeDescriptor<?>) o);
			}
		}
	}

	@Override
	public void addPages() {
		super.addPages();

		this.page = new ReplicateWizardPageOne();
		addPage(this.page);
	}

	@Override
	public boolean performFinish() {
		for (int i = 0; i < page.getReplicatesCount(); i++) {
			for (NodeDescriptor<?> descriptor : selection) {
				duplicate(descriptor.getParent(), descriptor);
			}
		}

		return true;
	}

	private void duplicate(ConfigurationElement<?> parent, ConfigurationElement<?> descriptor) {
		AddNodeOperation operation = createAddOperation((NodeDescriptor<?>) parent, (NodeDescriptor<?>) descriptor);

		try {
			execute(operation);

			for (ConfigurationElement<?> child : descriptor.getChildren()) {
				duplicate(operation.getNode(), child);
			}
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private AddAgentOperation createAddOperation(NodeDescriptor<?> parent, NodeDescriptor<?> desc) {
		String factoryPid = desc.getFactoryPid();
		HashMap<String, Object> configuration = new HashMap<String, Object>(desc.getConfiguration());

		if (desc instanceof FPAIDeviceAgentNodeDescriptor) {
			return new AddFPAIAgentOperation(parent, factoryPid, configuration);
		} else if (desc instanceof ResourceManagerNodeDescriptor) {
			return new AddResourceManagerOperation(parent, factoryPid, configuration);
		} else if (desc instanceof ResourceDriverNodeDescriptor) {
			return new AddResourceModelOperation(parent, factoryPid, configuration);
		} else if (desc instanceof DeviceAgentNodeDescriptor) {
			return new AddAgentOperation(parent, factoryPid, configuration);
		} else if (desc instanceof ConcentratorNodeDescriptor) {
			return new AddConcentratorOperation(parent, factoryPid, configuration);
		} else {
			throw new IllegalArgumentException("Unsupported node descriptor type: " + desc.getClass().getName());
		}
	}

	private void execute(IUndoableOperation operation) throws ExecutionException {
		IWorkbench workbench = PlatformUI.getWorkbench();
		operation.addContext(workbench.getOperationSupport().getUndoContext());
		workbench.getOperationSupport().getOperationHistory().execute(operation, null, null);
	}
}
