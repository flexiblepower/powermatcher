/*******************************************************************************
 * Copyright (c) 2010 Jean-Michel Lemieux, Jeff McAffer, Chris Aniszczyk and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Hyperbola is an RCP application developed for the book
 *     Eclipse Rich Client Platform - 
 *         Designing, Coding, and Packaging Java Applications
 * See http://eclipsercp.org
 *
 * Contributors:
 *     Jean-Michel Lemieux and Jeff McAffer - initial API and implementation
 *     Chris Aniszczyk - edits for the second edition
 *******************************************************************************/
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

/**
 * Add roster entry dialog, which prompts for the entry details.
 */
public class AddAgentDialog extends Dialog {

	private Text agentIdText;

	private Text agentNameText;

	private String agentId;

	private String agentName;

	public AddAgentDialog(Shell parentShell) {
		super(parentShell);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Add Agent Entry");
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		composite.setLayout(layout);

		Label userIdLabel = new Label(composite, SWT.NONE);
		userIdLabel.setText("&Agent id:");
		userIdLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER,
				false, false));

		agentIdText = new Text(composite, SWT.BORDER);
		agentIdText.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
				true, false));

		Label nicknameLabel = new Label(composite, SWT.NONE);
		nicknameLabel.setText("&Agent name:");
		nicknameLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER,
				false, false));

		agentNameText = new Text(composite, SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL, GridData.FILL, true,
				false);
		gridData.widthHint = convertHeightInCharsToPixels(20);
		agentNameText.setLayoutData(gridData);

		return composite;
	}

	protected void okPressed() {
		agentName= agentNameText.getText();
		agentId = agentIdText.getText();

		if (agentName.equals("")) {
			MessageDialog.openError(getShell(), "Invalid agent name",
					"agent name field must not be blank.");
			return;
		}
		if (agentId.equals("")) {
			MessageDialog.openError(getShell(), "Invalid Agent id",
					"Agent id field must not be blank.");
			return;
		}

		super.okPressed();
	}

	public String getAgentId() {
		return agentId;
	}

		public String getAgentName() {
		return agentName;
	}
}
