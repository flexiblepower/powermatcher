package net.powermatcher.simulation.gui.operations;

import java.util.List;

import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.gui.ComponentCountManager;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class RemoveNodeOperation extends AbstractOperation {
	private NodeDescriptor parent;
	private NodeDescriptor child;

	private enum Op {
		REMOVE, ADD
	};

	public RemoveNodeOperation(NodeDescriptor child) {
		super("Remove node operation");
		this.parent = (NodeDescriptor) child.getParent();
		this.child = child;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		parent.removeChild(child);
		adjustReuseCount(child, Op.REMOVE);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return execute(monitor, info);
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		parent.addChild(child);
		adjustReuseCount(child, Op.ADD);
		return Status.OK_STATUS;
	}

	private void adjustReuseCount(NodeDescriptor nd, Op operation) {
		addReuseCount(nd, operation);

		for (NodeDescriptor nds : (List<NodeDescriptor>) nd.getChildren()) {
			adjustReuseCount(nds, operation);
		}
	}

	private void addReuseCount(NodeDescriptor item, Op operation) {
		int number = getNumber(item.getId());
		if (number == -1) {
			return;
		}

		ComponentCountManager countManager = ComponentCountManager.getInstance();

		switch (operation) {
		case REMOVE:
			countManager.addReusableNumber(item.getClass(), number);
			break;
		case ADD:
			countManager.removeUsableNumber(item.getClass(), number);
			break;
		}
	}

	private int getNumber(String id) {
		int index = id.lastIndexOf("-");

		if (index == -1) {
			return index;
		}

		return Integer.valueOf(id.substring(index + 1, id.length()));
	}
}
