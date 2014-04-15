package net.powermatcher.simulation.gui.handlers;

import net.powermatcher.simulation.gui.views.ConfigurationView;
import net.powermatcher.simulation.gui.wizards.ReplicateWizard;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class Replicate extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ReplicateWizard wizard = new ReplicateWizard();
		WizardDialog dialog = new WizardDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), wizard);

		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
		ConfigurationView view = (ConfigurationView) page.findView(ConfigurationView.ID);
		IStructuredSelection selection = (IStructuredSelection) view.getSite().getSelectionProvider().getSelection();
		
		wizard.init(PlatformUI.getWorkbench(), selection);

		dialog.open();

		return null;
	}
}
