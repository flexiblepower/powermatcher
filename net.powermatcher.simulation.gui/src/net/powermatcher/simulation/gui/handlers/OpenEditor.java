package net.powermatcher.simulation.gui.handlers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.configuration.ScenarioDescriptor;
import net.powermatcher.simulation.gui.Application;
import net.powermatcher.simulation.gui.GUIUtils;
import net.powermatcher.simulation.gui.editors.AgentEditorInput;
import net.powermatcher.simulation.gui.editors.AgentInfoEditor;
import net.powermatcher.simulation.gui.editors.ScenarioEditorInput;
import net.powermatcher.simulation.gui.editors.ScenarioInfoEditor;
import net.powermatcher.simulation.gui.views.ConfigurationView;
import net.powermatcher.simulation.telemetry.metadata.TelemetryMetaData;
import net.powermatcher.simulation.telemetry.metadata.TelemetryMetaDataService;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.metatype.MetaTypeInformation;
import org.osgi.service.metatype.MetaTypeService;
import org.osgi.service.metatype.ObjectClassDefinition;

public class OpenEditor extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the page and view
		IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
		ConfigurationView view = (ConfigurationView) page.findView(ConfigurationView.ID);

		// Get the selection
		ISelection selection = view.getSite().getSelectionProvider().getSelection();

		// open the editor
		if (selection != null && selection instanceof IStructuredSelection) {
			Object item = ((IStructuredSelection) selection).getFirstElement();

			if (item instanceof NodeDescriptor) {
				openNodeEditor(page, (NodeDescriptor) item);
			} else if (item instanceof ScenarioDescriptor) {
				openScenarioEditor(page, (ScenarioDescriptor) item);
			} else {
				MessageDialog.openWarning(Application.getInstance().getShell(),
						"Can't display properties", "The properties could not be displayed for this selection");
			}
		}
		return null;
	}

	private void openNodeEditor(IWorkbenchPage page, NodeDescriptor entry) {
		if (focusOpenNodeEditor(page, entry)) {
			return;
		}

		ObjectClassDefinition classDefinition = extractOCDInformation(entry);
		TelemetryMetaData telemetryData = extractTelemetryMetaData(entry);
		AgentEditorInput agentEditorInput = new AgentEditorInput(entry, classDefinition, telemetryData);

		try {
			page.openEditor(agentEditorInput, AgentInfoEditor.ID);
		} catch (PartInitException e) {
			Writer result = new StringWriter();
			e.printStackTrace(new PrintWriter(result));
			MessageDialog.openWarning(Application.getInstance().getShell(),
					"Can't display properties", "The properties could not be displayed:\n" + e.getMessage() + "\n"
							+ result.toString());
		}
	}

	private boolean focusOpenNodeEditor(IWorkbenchPage page, NodeDescriptor entry) {
		for (IEditorReference ref : page.getEditorReferences()) {
			IEditorInput editorInput = null;

			try {
				editorInput = ref.getEditorInput();
			} catch (PartInitException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				continue;
			}

			if (editorInput instanceof AgentEditorInput
					&& ((AgentEditorInput) editorInput).getNodeDescriptor() == entry) {
				page.bringToTop(ref.getPart(true));
				return true;
			}
		}

		return false;
	}

	private void openScenarioEditor(IWorkbenchPage page, ScenarioDescriptor entry) {
		if (focusOpenScenarioEditor(page, entry)) {
			return;
		}

		try {
			ScenarioEditorInput scenarioEditorInput = new ScenarioEditorInput(entry);
			page.openEditor(scenarioEditorInput, ScenarioInfoEditor.ID);
		} catch (PartInitException e) {
			e.printStackTrace();
		}
	}

	private boolean focusOpenScenarioEditor(IWorkbenchPage page, ScenarioDescriptor entry) {
		// Check if the window is already open
		for (IEditorReference ref : page.getEditorReferences()) {
			IEditorInput editorInput = null;

			try {
				editorInput = ref.getEditorInput();
			} catch (PartInitException e) {
				e.printStackTrace();
				continue;
			}

			// check if it is the same object
			if (editorInput instanceof ScenarioEditorInput
					&& ((ScenarioEditorInput) editorInput).getScenarioDescriptor() == entry) {
				page.bringToTop(ref.getPart(true));
				return true;
			}
		}

		return false;
	}

	private ObjectClassDefinition extractOCDInformation(NodeDescriptor entry) {
		MetaTypeService service = Application.getInstance().getMetaTypeService();
		GUIUtils guiUtils = GUIUtils.getInstance();

		Bundle bundle = FrameworkUtil.getBundle(this.getClass());
		BundleContext context = bundle.getBundleContext();

		for (Bundle b : context.getBundles()) {
			MetaTypeInformation information = service.getMetaTypeInformation(b);

			// skip over meta type information without factory pids
			if (information.getFactoryPids().length == 0) {
				continue;
			}

			for (String fpid : information.getFactoryPids()) {
				// skip over factories with a different displayable name
				if (guiUtils.getDisplayableName(fpid).equalsIgnoreCase(guiUtils.getDisplayableName(entry))) {
					return information.getObjectClassDefinition(fpid, null);
				}
			}

		}

		return null;
	}

	private TelemetryMetaData extractTelemetryMetaData(NodeDescriptor entry) {
		try {
			TelemetryMetaDataService telemetryService = Application.getInstance().getTelemetryMetaDataService();
			Bundle bundle = FrameworkUtil.getBundle(Class.forName(entry.getFactoryPid()));
			return telemetryService.getTelemetryMetaData(bundle);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
}
