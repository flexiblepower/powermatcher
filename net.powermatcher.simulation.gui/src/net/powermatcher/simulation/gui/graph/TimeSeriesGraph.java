package net.powermatcher.simulation.gui.graph;

import org.csstudio.swt.xygraph.figures.Axis;
import org.csstudio.swt.xygraph.figures.XYGraph;

public abstract class TimeSeriesGraph extends XYGraph {
	private final Axis timeAxis;

	public TimeSeriesGraph() {
		timeAxis = this.primaryXAxis;
		timeAxis.setDateEnabled(true);
		timeAxis.setAutoScale(true);
		timeAxis.setAutoScaleThreshold(0);
		timeAxis.setTitle("Time");
	}

	public Axis getTimeAxis() {
		return timeAxis;
	}
}
