package net.powermatcher.simulation.gui.wizards;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ReplicateWizardPageOne extends WizardPage {
	private Text countText;
	private Composite container;

	public ReplicateWizardPageOne() {
		super("Replicate Wizard");
		setTitle("Replicate Wizard");
		setDescription("A wizard to replicate a sub-cluster");
	}

	@Override
	public void createControl(Composite parent) {
		container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(2, false));

		Label countLabel = new Label(container, SWT.NULL);
		countLabel.setText("Enter number of replicates:");

		countText = new Text(container, SWT.BORDER | SWT.SINGLE);
		countText.setText("1");
		countText.setSelection(0, 1);
		countText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		countText.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent event) {
			}

			@Override
			public void keyReleased(KeyEvent event) {
				if (countText.getText().isEmpty()) {
					setPageComplete(false);
					return;
				}

				try {
					Double.parseDouble(countText.getText());
					setPageComplete(true);
				} catch (NumberFormatException exception) {
					MessageDialog.openError(getShell(), "Invalid Number", "The number you entered is not valid");
					setPageComplete(false);
				}
			}
		});

		setControl(container);
	}

	public int getReplicatesCount() {
		return Integer.parseInt(countText.getText());
	}
}
