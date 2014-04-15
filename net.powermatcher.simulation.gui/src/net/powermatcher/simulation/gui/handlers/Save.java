package net.powermatcher.simulation.gui.handlers;

import java.io.File;

import net.powermatcher.simulation.configuration.XmlSerializer;
import net.powermatcher.simulation.gui.Application;
import net.powermatcher.simulation.gui.ScenarioContainer;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class Save extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ScenarioContainer scenarioContainer = Application.getInstance().getScenarios();

		if (scenarioContainer.hasChildren() == false) {
			Application.getInstance().getDisplay().asyncExec(new Runnable() {
				public void run() {
					MessageDialog.openWarning(Application.getInstance().getShell(), "Save failed",
							"There is no active scenario to save");
				}
			});

			return null;
		}

		save(scenarioContainer);

		return null;
	}

	private void save(ScenarioContainer scenarioContainer) {
		FileDialog fileDialog = createDialog();

		String filename = fileDialog.open();
		System.out.println("Printing  here:" + filename);
		saveFile(filename, scenarioContainer);

	}

	private void saveFile(String filename, ScenarioContainer scenarioContainer) {
		if (filename != null) {
			try {
				File outputFile = new File(filename);
				if (outputFile.exists() && outputFile.isDirectory()) {
					Application.getInstance().getDisplay().asyncExec(new Runnable() {
						public void run() {
							MessageDialog.openWarning(Application.getInstance().getShell(), "Could not save scenario",
									"An error occured while trying to save the scenario");
						}
					});
					return;
				} else

				if (outputFile.exists()) {
					if (MessageDialog.openConfirm(Application.getInstance().getShell(), "File already exists",
							"Do you want to override the file \"" + filename + "\"?")) {
						XmlSerializer.saveScenario(scenarioContainer.getActiveScenario(), outputFile);
					} else {
						FileDialog fileDialog = createDialog();

						String newName = fileDialog.open();
						System.out.println("New Name:" + newName);
						saveFile(newName, scenarioContainer);
					}
				} else {
					XmlSerializer.saveScenario(scenarioContainer.getActiveScenario(), outputFile);
				}
			} catch (Exception e) {
				Application.getInstance().getShell().getDisplay().asyncExec(new Runnable() {
					public void run() {

						MessageDialog.openWarning(Application.getInstance().getShell(), "Could not save scenario",
								"An error occured while trying to save the scenario");
					}
				});
			}
		}
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
}
