package net.powermatcher.simulation.gui;

import net.powermatcher.simulation.configuration.ConfigurationElement;
import net.powermatcher.simulation.configuration.ConfigurationElementImpl;
import net.powermatcher.simulation.configuration.ScenarioDescriptor;

public class ScenarioContainer extends ConfigurationElementImpl<ScenarioDescriptor> {
	private ScenarioDescriptor activeScenario;

	public ScenarioDescriptor getActiveScenario() {
		return activeScenario;
	}

	@Override
	public void setParent(ConfigurationElement<?> parent) {
		return;
	}

	@Override
	public boolean removeChild(ScenarioDescriptor child) {
		boolean succes = super.removeChild(child);

		// if it was the active scenario, select another
		if (this.activeScenario.equals(child)) {
			if(this.getChildren().isEmpty()){
				this.activeScenario = null;
			} else{
				setActiveScenario(this.getChildren().get(0));
			}
			
		}

		return succes;
	}

	public void setActiveScenario(ScenarioDescriptor activeScenario) {
		if (this.activeScenario != null) {
			this.activeScenario.setPassive();
		}

		this.activeScenario = activeScenario;
		this.activeScenario.setActive();
		this.observers.notifyChildAdded(this, activeScenario);
	}
}
