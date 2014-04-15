package net.powermatcher.simulation.gui.handlers;

import net.powermatcher.simulation.engine.SimulationControl;
import net.powermatcher.simulation.gui.Application;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class Step extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		SimulationControl control = Application.getInstance().getSimulationControl();
		if (control != null)
			control.step();
		return null;
	}
}