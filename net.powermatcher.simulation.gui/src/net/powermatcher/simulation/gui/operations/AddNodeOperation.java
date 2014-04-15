package net.powermatcher.simulation.gui.operations;

import java.util.HashMap;

import net.powermatcher.simulation.configuration.NodeDescriptor;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public abstract class AddNodeOperation extends AbstractOperation {
	private final String factoryId;
	private final NodeDescriptor<NodeDescriptor<?>> parent;

	private NodeDescriptor<?> node;
	private HashMap<String, Object> configuration;

	public AddNodeOperation(String title, NodeDescriptor<NodeDescriptor<?>> parent, String factoryId, HashMap<String, Object> configuration) {
		super(title);

		this.parent = parent;
		this.factoryId = factoryId;
		this.configuration = configuration;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		this.node = createNode(this.parent, this.factoryId);
		this.parent.addChild(this.node);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		this.parent.addChild(this.node);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		this.parent.removeChild(this.node);
		return Status.OK_STATUS;
	}

	protected NodeDescriptor<?> createNode(NodeDescriptor<NodeDescriptor<?>> parent, String factoryPid) {
		NodeDescriptor<?> node = createDescriptor();
		node.setFactoryPid(factoryPid);

		if (this.configuration != null) {
			node.setConfiguration(this.configuration);
		}

		return node;
	}

	protected abstract NodeDescriptor<?> createDescriptor();

	public NodeDescriptor<?> getNode() {
		return node;
	}
}
