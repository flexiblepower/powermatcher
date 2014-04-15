package net.powermatcher.simulation.gui.menus;

import java.util.ArrayList;
import java.util.List;

import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.gui.Application;
import net.powermatcher.simulation.gui.GUIUtils;
import net.powermatcher.simulation.gui.views.ConfigurationView;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.metatype.MetaTypeInformation;
import org.osgi.service.metatype.MetaTypeService;

public abstract class AddNodeMenuContribution extends ContributionItem {
	List<String> componentFactoryPids = new ArrayList<String>();

	public AddNodeMenuContribution() {
	}

	public AddNodeMenuContribution(String id) {
		super(id);
	}

	@Override
	public boolean isDynamic() {
		return true;
	}

	@Override
	public void fill(Menu menu, int index) {
		componentFactoryPids.clear();

		MetaTypeService service = Application.getInstance().getMetaTypeService();
		BundleContext context = FrameworkUtil.getBundle(getClass()).getBundleContext();

		for (Bundle b : context.getBundles()) {
			MetaTypeInformation information = service.getMetaTypeInformation(b);
			if (information.getFactoryPids().length > 0) {
				for (String fpid : information.getFactoryPids()) {
					try {
						Class<?> clazz = b.loadClass(fpid);

						if (filterComponentClass(clazz)) {
							componentFactoryPids.add(fpid);
						}
					} catch (ClassNotFoundException e) {
						continue;
					}
				}
			}
		}

		for (String componentFactoryPid : componentFactoryPids) {
			MenuItem menuItem = new MenuItem(menu, SWT.MENU, index);
			menuItem.setText(GUIUtils.getInstance().getDisplayableName(componentFactoryPid));

			menuItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (e.getSource() instanceof MenuItem) {
						String selection = ((MenuItem) e.getSource()).getText();
						createAgent(selection);
					}
				}
			});
		}
	}

	/**
	 * filter components by class
	 * 
	 * @return true to show and false to hide
	 */
	protected abstract boolean filterComponentClass(Class<?> componentClass);

	protected void createAgent(String selection) {
		for (String componentFactoryPid : componentFactoryPids) {
			if (GUIUtils.getInstance().getDisplayableName(componentFactoryPid).equals(selection)) {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				ConfigurationView view = (ConfigurationView) page.findView(ConfigurationView.ID);
				IStructuredSelection selection1 = (IStructuredSelection) view.getSite().getSelectionProvider().getSelection();
				createComponent(componentFactoryPid, (NodeDescriptor) selection1.getFirstElement());
			}
		}

	}

	protected abstract void createComponent(String componentFactoryPid, NodeDescriptor parent);
}
