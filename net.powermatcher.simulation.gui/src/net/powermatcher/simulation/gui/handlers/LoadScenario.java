package net.powermatcher.simulation.gui.handlers;

import java.io.File;

import net.powermatcher.simulation.configuration.ScenarioDescriptor;
import net.powermatcher.simulation.configuration.XmlSerializer;
import net.powermatcher.simulation.gui.Application;
import net.powermatcher.simulation.gui.ComponentCountManager;
import net.powermatcher.simulation.gui.ScenarioContainer;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class LoadScenario extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final FileDialog fileDialog = createDialog();
		final String fileName = fileDialog.open();

		// an error occurred or the dialog was cancelled
		if (fileName == null) {
			return null;
		}

		loadScenario(fileName);

		return null;
	}

	private FileDialog createDialog() {
		Shell shell = Application.getInstance().getShell();

		final FileDialog fileDialog = new FileDialog(shell, SWT.OPEN);
		fileDialog.setText("Choose a Scenario file");
		fileDialog.setFilterNames(new String[] { "Scenario Files", "All Files (*)" });
		fileDialog.setFilterExtensions(new String[] { "*.xml", "*" });
		fileDialog.setFilterPath("/");

		return fileDialog;
	}

	private void loadScenario(final String fileName) {
		try {
			final File inputFile = new File(fileName);
			final ScenarioDescriptor loadScenario = XmlSerializer.loadScenario(inputFile);
			final ScenarioContainer scenarios = Application.getInstance().getScenarios();

			// Check whether the same scenario is already loaded in the UI
			for (final ScenarioDescriptor scenario : scenarios.getChildren()) {
				if (scenario.scenarioIsSaved() && scenario.getFile().equals(inputFile)) {
					Application.getInstance().getDisplay().asyncExec(new Runnable() {
						public void run() {
							boolean reload = MessageDialog.openQuestion(Application.getInstance().getShell(),
									"Scenario already open", "The scenario \"" + inputFile.getName()
											+ "\" is already open. Do you want to reload this scenario?"
											+ " Any changes to this scenario will be lost.");

							if (reload) {
								scenarios.removeChild(scenario);
								scenarios.addChild(loadScenario);
								scenarios.setActiveScenario(loadScenario);
								ComponentCountManager.getInstance().updateCurrentData();
							}
						}
					});

					return;
				}
			}

			scenarios.addChild(loadScenario);
			scenarios.setActiveScenario(loadScenario);
			ComponentCountManager.getInstance().updateCurrentData();
		} catch (Exception e) {
			Application.getInstance().getDisplay().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openWarning(Application.getInstance().getShell(), "Could not open file",
							"The file \"" + fileName + "\" could not be loaded");
				}
			});
		}
	}
}
