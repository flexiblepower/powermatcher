package net.powermatcher.simulation.gui;

import net.powermatcher.simulation.configuration.NodeDescriptor;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class PMSimulationAdapterFactory implements IAdapterFactory {

	private IWorkbenchAdapter nodeDescriptorAdapter = new IWorkbenchAdapter() {
		public Object getParent(Object o) {
			return ((NodeDescriptor) o).getParent();
		}

		public String getLabel(Object o) {
			NodeDescriptor group = ((NodeDescriptor) o);

			if (group.hasChildren()) {
				int lastIndexOf = group.getFactoryPid().lastIndexOf(".");
				return group.getFactoryPid().substring(lastIndexOf + 1, group.getFactoryPid().length());
			} else {
				int lastIndexOf = group.getFactoryPid().lastIndexOf(".");
				return group.getFactoryPid().substring(lastIndexOf + 1, group.getFactoryPid().length());
			}
		}

		public ImageDescriptor getImageDescriptor(Object object) {
			return null;
		}

		public Object[] getChildren(Object o) {
			return ((NodeDescriptor) o).getChildren().toArray();
		}
	};

	@SuppressWarnings("rawtypes")
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (adapterType == IWorkbenchAdapter.class && adaptableObject instanceof NodeDescriptor)
			return nodeDescriptorAdapter;

		return null;
	}

	@SuppressWarnings("rawtypes")
	public Class[] getAdapterList() {
		return new Class[] { IWorkbenchAdapter.class };
	}

}
