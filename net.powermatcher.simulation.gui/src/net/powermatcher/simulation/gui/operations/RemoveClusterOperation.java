package net.powermatcher.simulation.gui.operations;

import net.powermatcher.simulation.configuration.ClusterDescriptor;
import net.powermatcher.simulation.configuration.ScenarioDescriptor;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class RemoveClusterOperation extends AbstractOperation {
	private ScenarioDescriptor parent;
	private ClusterDescriptor clusterDescriptor;

	public RemoveClusterOperation(ClusterDescriptor clusterDescriptor) {
		super("Remove Cluster Operation");
		this.clusterDescriptor = clusterDescriptor;
		this.parent = clusterDescriptor.getParent();
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		parent.removeChild(clusterDescriptor);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		parent.addChild(clusterDescriptor);
		return Status.OK_STATUS;
	}

}