package net.powermatcher.simulation.gui;

import net.powermatcher.simulation.gui.views.ConfigurationView;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {
	public void createInitialLayout(IPageLayout layout) {
		layout.setEditorAreaVisible(true);
		layout.setFixed(true);
		layout.addView(ConfigurationView.ID, IPageLayout.LEFT, 0.25f, layout.getEditorArea());
		layout.getViewLayout(ConfigurationView.ID).setCloseable(false);
	}
}
