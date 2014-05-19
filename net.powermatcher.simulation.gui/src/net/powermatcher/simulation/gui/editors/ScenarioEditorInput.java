package net.powermatcher.simulation.gui.editors;

import net.powermatcher.simulation.configuration.ScenarioDescriptor;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class ScenarioEditorInput implements IEditorInput {

	private final ScenarioDescriptor scenarioDescriptor;

	public ScenarioEditorInput(ScenarioDescriptor entry) {
		this.scenarioDescriptor = entry;
	}

	@Override
	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return this.scenarioDescriptor.getScenarioId();
	}

	@Override
	public IPersistableElement getPersistable() {
		// TODO Auto-generated method stub
		return null;
	}

	public ScenarioDescriptor getScenarioDescriptor() {
		return this.scenarioDescriptor;
	}

	@Override
	public String getToolTipText() {
		return "Scenario editor";
	}

}
