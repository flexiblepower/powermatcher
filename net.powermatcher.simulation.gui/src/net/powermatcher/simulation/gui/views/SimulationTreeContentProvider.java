package net.powermatcher.simulation.gui.views;

import net.powermatcher.simulation.configuration.ConfigurationElement;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class SimulationTreeContentProvider implements ITreeContentProvider {
	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ConfigurationElement) {
			return ((ConfigurationElement) parentElement).getChildren().toArray();
		} else {
			return null;
		}
	}

	@Override
	public Object getParent(Object inputElement) {
		// if (inputElement instanceof NodeDescriptor) {
		// return ((NodeDescriptor) inputElement).getParent();
		// }

		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof ConfigurationElement) {
			return ((ConfigurationElement) element).hasChildren();
		} else {
			return false;
		}
	}

	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
