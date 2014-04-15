package net.powermatcher.simulation.gui.graph;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.swt.widgets.Display;

public class GraphUpdater {
	private int updateInterval = 100; // milliseconds

	private final List<UpdatableGraph> graphs = new CopyOnWriteArrayList<UpdatableGraph>();

	public GraphUpdater() {
	}

	public GraphUpdater(UpdatableGraph... graphs) {
		this.graphs.addAll(Arrays.asList(graphs));
	}

	public void start(final Display display) {
		display.timerExec(0, new Updater(display));
	}

	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
	}

	private final class Updater implements Runnable {
		private final Display display;

		private Updater(Display display) {
			this.display = display;
		}

		public void run() {
			display.timerExec(updateInterval, this);
			for (UpdatableGraph graph : graphs) {
				graph.updateGraph();
			}
		}
	}

	public void add(UpdatableGraph graph) {
		graphs.add(graph);
	}

	public void remove(UpdatableGraph graph) {
		graphs.remove(graph);
	}
}
