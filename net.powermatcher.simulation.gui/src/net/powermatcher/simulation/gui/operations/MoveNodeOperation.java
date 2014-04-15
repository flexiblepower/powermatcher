package net.powermatcher.simulation.gui.operations;

import net.powermatcher.simulation.configuration.NodeDescriptor;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class MoveNodeOperation extends AbstractOperation {
	private NodeDescriptor source;
	private NodeDescriptor child;
	private NodeDescriptor target;

	public MoveNodeOperation(NodeDescriptor child, NodeDescriptor target) {
		super("Remove node operation");
		this.source = (NodeDescriptor) child.getParent();
		this.child = child;
		this.target = target;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		source.removeChild(child);
		target.addChild(child);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		source.addChild(child);
		target.removeChild(child);
		return Status.OK_STATUS;
	}
}
