package net.powermatcher.simulation.gui.views;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.bind.JAXBException;

import net.powermatcher.simulation.configuration.ClusterDescriptor;
import net.powermatcher.simulation.configuration.ConcentratorNodeDescriptor;
import net.powermatcher.simulation.configuration.ConfigurationElement;
import net.powermatcher.simulation.configuration.ConfigurationElementObserver;
import net.powermatcher.simulation.configuration.DeviceAgentNodeDescriptor;
import net.powermatcher.simulation.configuration.FPAIDeviceAgentNodeDescriptor;
import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.configuration.ResourceDriverNodeDescriptor;
import net.powermatcher.simulation.configuration.ResourceManagerNodeDescriptor;
import net.powermatcher.simulation.configuration.ScenarioDescriptor;
import net.powermatcher.simulation.configuration.XmlSerializer;
import net.powermatcher.simulation.gui.Application;
import net.powermatcher.simulation.gui.ComponentCountManager;
import net.powermatcher.simulation.gui.PMSimulationAdapterFactory;
import net.powermatcher.simulation.gui.ScenarioContainer;
import net.powermatcher.simulation.gui.handlers.OpenEditor;
import net.powermatcher.simulation.gui.operations.AddAgentOperation;
import net.powermatcher.simulation.gui.operations.AddConcentratorOperation;
import net.powermatcher.simulation.gui.operations.AddFPAIAgentOperation;
import net.powermatcher.simulation.gui.operations.AddNodeOperation;
import net.powermatcher.simulation.gui.operations.AddResourceManagerOperation;
import net.powermatcher.simulation.gui.operations.AddResourceModelOperation;
import net.powermatcher.simulation.gui.operations.MoveNodeOperation;
import net.powermatcher.simulation.gui.operations.RemoveNodeOperation;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.part.ViewPart;

public class ConfigurationView extends ViewPart {
	public static final String ID = ConfigurationView.class.getName().toLowerCase();

	private final IAdapterFactory adapterFactory = new PMSimulationAdapterFactory();

	private final ConfigurationTreeRefresh configurationTreeRefresh = new ConfigurationTreeRefresh();

	private MenuManager menuManager;

	private TreeViewer treeViewer;

	@Override
	public void createPartControl(Composite parent) {
		ScenarioContainer scenarioContainer = Application.getInstance().getScenarios();
		addTreeRefresh(scenarioContainer);

		treeViewer = new TreeViewer(parent, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		getSite().setSelectionProvider(treeViewer);
		Platform.getAdapterManager().registerAdapters(adapterFactory, NodeDescriptor.class);
		treeViewer.setLabelProvider(new SimulationTreeLabelProvider());
		treeViewer.setContentProvider(new SimulationTreeContentProvider());
		treeViewer.setInput(scenarioContainer);

		// add drag and drop support
		int dndOperations = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] { new ConfigurationElementTransfer() };
		treeViewer.addDragSupport(dndOperations, transfers, new DragSourceListenerImpl());
		treeViewer.addDropSupport(dndOperations, transfers, new DropTargetListenerImpl());

		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
				try {
					handlerService.executeCommand(OpenEditor.class.getName(), null);
				} catch (Exception ex) {
					throw new RuntimeException(OpenEditor.class.getName() + " not found");
				}
			}
		});

		menuManager = new MenuManager();

		treeViewer.getTree().setMenu(menuManager.createContextMenu(treeViewer.getTree()));
		getSite().registerContextMenu(menuManager, treeViewer);
		getSite().setSelectionProvider(treeViewer);
	}

	@Override
	public void dispose() {
		Platform.getAdapterManager().unregisterAdapters(adapterFactory);
		super.dispose();
	}

	@Override
	public void setFocus() {
		treeViewer.getControl().setFocus();
	}

	private void addTreeRefresh(ConfigurationElement<?> configurationElement) {
		configurationElement.addObserver(configurationTreeRefresh);

		for (ConfigurationElement<?> child : configurationElement.getChildren()) {
			addTreeRefresh(child);
		}
	}

	private void removeTreeRefresh(ConfigurationElement<?> configurationElement) {
		configurationElement.removeObserver(configurationTreeRefresh);

		for (ConfigurationElement<?> child : configurationElement.getChildren()) {
			removeTreeRefresh(child);
		}
	}

	private final class ConfigurationTreeRefresh implements ConfigurationElementObserver {
		@Override
		public void notifyChanged(final ConfigurationElement<?> element) {
			ComponentCountManager.getInstance().updateCurrentData();
			treeViewer.getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					treeViewer.refresh(element);
				}
			});
		}

		@Override
		public void notifyChildAdded(final ConfigurationElement<?> parent, final ConfigurationElement<?> child) {
			addTreeRefresh(child);
			ComponentCountManager.getInstance().updateCurrentData();
			treeViewer.getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					treeViewer.refresh(parent);
					treeViewer.expandToLevel(parent, 1);

					if (child instanceof ScenarioDescriptor) {
						treeViewer.expandToLevel(parent, 4);
					}
					if (child instanceof ClusterDescriptor) {
						treeViewer.expandToLevel(parent, 2);
					}
				}
			});
		}

		@Override
		public void notifyChildRemoved(final ConfigurationElement<?> parent, ConfigurationElement<?> child) {
			removeTreeRefresh(child);

			// TODO this should be here, nothing to do with the configuration
			// view ... should be responsibility of the editor right?
			closeEditor(child);

			ComponentCountManager.getInstance().updateCurrentData();
			treeViewer.getControl().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					treeViewer.refresh(parent);
				}
			});
		}

		// TODO this should be here, nothing to do with the configuration view
		// ... should be responsibility of the editor right?
		private void closeEditor(ConfigurationElement<?> element) {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

			if (element.hasChildren()) {
				List<? extends ConfigurationElement<?>> children = element.getChildren();

				for (ConfigurationElement<?> child : children) {
					closeEditor(child);
				}
			}

			String name = null;
			if (element instanceof NodeDescriptor) {
				name = ((NodeDescriptor<?>) element).getId();
			} else if (element instanceof ScenarioDescriptor) {
				name = ((ScenarioDescriptor) element).getScenarioId();
			} else if (element instanceof ClusterDescriptor) {
				name = ((ClusterDescriptor) element).getClusterId();
			}

			IEditorReference[] references = page.getEditorReferences();
			for (IEditorReference ref : references) {
				try {
					if (name.equals(ref.getEditorInput().getName())) {
						page.closeEditor(ref.getEditor(true), true);
					}
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private final class DragSourceListenerImpl implements DragSourceListener {
		@Override
		public void dragStart(DragSourceEvent event) {
		}

		@Override
		public void dragSetData(DragSourceEvent event) {
			List<Object> selectionData = new ArrayList<Object>();
			for (TreeItem item : ((Tree) ((DragSource) event.getSource()).getControl()).getSelection()) {
				selectionData.add(item.getData());
			}

			Object[] array = selectionData.toArray(new Object[selectionData.size()]);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			try {
				// TODO marhall all, not just the first
				XmlSerializer.marshal((ConfigurationElement<?>) array[0], baos);
				event.data = baos.toByteArray();
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}

		@Override
		public void dragFinished(DragSourceEvent event) {
			if (event.detail == DND.DROP_COPY) {
				return;
			}

			for (TreeItem item : ((Tree) ((DragSource) event.getSource()).getControl()).getSelection()) {
				try {
					RemoveNodeOperation operation = new RemoveNodeOperation((NodeDescriptor<?>) item.getData());

					IWorkbench workbench = PlatformUI.getWorkbench();
					operation.addContext(workbench.getOperationSupport().getUndoContext());
					workbench.getOperationSupport().getOperationHistory().execute(operation, null, null);
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private final class DropTargetListenerImpl implements DropTargetListener {
		@Override
		public void dropAccept(DropTargetEvent event) {
		}

		@SuppressWarnings("rawtypes")
		@Override
		public void drop(DropTargetEvent event) {
			try {
				ConfigurationElement<?> element = XmlSerializer
						.unmarshal(new ByteArrayInputStream((byte[]) event.data));

				TreeItem target = ((TreeItem) event.item);
				if (target == null) {
					return;
				}

				if (event.detail == DND.DROP_COPY || event.detail == DND.DROP_MOVE) {
					duplicate((NodeDescriptor) target.getData(), (NodeDescriptor) element);
				} else {
					throw new UnsupportedOperationException();
				}
			} catch (JAXBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public void dragOver(DropTargetEvent event) {
		}

		@Override
		public void dragOperationChanged(DropTargetEvent event) {
		}

		@Override
		public void dragLeave(DropTargetEvent event) {
		}

		@Override
		public void dragEnter(DropTargetEvent event) {
		}

		// TODO this code is duplicated from AddNodeOperation, it shouldn't
		private void duplicate(ConfigurationElement<?> parent, ConfigurationElement<?> descriptor) {
			AddNodeOperation operation = createAddOperation((NodeDescriptor<?>) parent, (NodeDescriptor<?>) descriptor);

			try {
				execute(operation);
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// TODO this code is duplicated from AddNodeOperation, it shouldn't
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

		// TODO this code is duplicated from AddNodeOperation, it shouldn't
		private void execute(IUndoableOperation operation) throws ExecutionException {
			IWorkbench workbench = PlatformUI.getWorkbench();
			operation.addContext(workbench.getOperationSupport().getUndoContext());
			workbench.getOperationSupport().getOperationHistory().execute(operation, null, null);
		}
	}
}