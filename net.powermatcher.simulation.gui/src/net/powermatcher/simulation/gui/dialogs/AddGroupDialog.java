package net.powermatcher.simulation.gui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class AddGroupDialog extends Dialog {
	
	private Text groudIdText;

	private Text groupNameText;

	private String groupId;

	private String groupName;
	public AddGroupDialog(Shell parentShell) {
		super(parentShell);
		// TODO Auto-generated constructor stub
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Add Group");
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		Label userIdLabel = new Label(composite, SWT.NONE);
		userIdLabel.setText("&Group id:");
		userIdLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER,
				false, false));

		groudIdText = new Text(composite, SWT.BORDER);
		groudIdText.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
				true, false));

		Label nicknameLabel = new Label(composite, SWT.NONE);
		nicknameLabel.setText("&Group name:");
		nicknameLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER,
				false, false));

		groupNameText = new Text(composite, SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true,
				false);
		gridData.widthHint = convertHeightInCharsToPixels(20);
		groupNameText.setLayoutData(gridData);

		return composite;
	}

	protected void okPressed() {
		groupName= groupNameText.getText();
		groupId = groudIdText.getText();

		if (groupName.equals("")) {
			MessageDialog.openError(getShell(), "Invalid Group name",
					"Group name field must not be blank.");
			return;
		}
		if (groupId.equals("")) {
			MessageDialog.openError(getShell(), "Invalid group id",
					"Group id field must not be blank.");
			return;
		}

		super.okPressed();
	}

	public String getAgentId() {
		return groupId;
	}

		public String getAgentName() {
		return groupName;
	}
}
