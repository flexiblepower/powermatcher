package net.powermatcher.simulation.gui;

import java.util.Date;

import net.powermatcher.simulation.engine.SimulationCycleListener;

import org.eclipse.jface.action.IStatusLineManager;

public class SimulationStatusLine implements SimulationCycleListener {
	private long lastUpdate = -1;
	private IStatusLineManager statusLineManager = null;

	public SimulationStatusLine() {
		Application.getInstance().getSimulationControl().addSimulationCycleListener(this);
	}

	private void setMessage(final String msg) {
		Application.getInstance().getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (SimulationStatusLine.this.statusLineManager != null) {
					SimulationStatusLine.this.statusLineManager.setMessage(msg);
				}
			}
		});
	}

	public void setStatusLineManager(IStatusLineManager statusLineManager) {
		this.statusLineManager = statusLineManager;
		this.setMessage("No simulation running");
	}

	@Override
	public void simulationCycleBegins(long timestamp) {
		long now = System.currentTimeMillis();
		if (this.lastUpdate == -1 || now - this.lastUpdate > 250) {
			this.setMessage("Simulation running: " + new Date(timestamp).toString());
			this.lastUpdate = now;
		}
	}

	@Override
	public void simulationCycleFinishes(long timestamp) {
		// Not interesting
	}

	@Override
	public void simulationFinished() {
		this.lastUpdate = -1;
		this.setMessage("Simulation finished");
	}

	@Override
	public void simulationStarts(long timestamp) {
		this.setMessage("Started at: " + new Date(timestamp).toString());
	}
}
