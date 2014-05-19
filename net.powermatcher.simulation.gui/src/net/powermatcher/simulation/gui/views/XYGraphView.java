package net.powermatcher.simulation.gui.views;

import net.powermatcher.simulation.gui.graph.BidTimeSeriesXYGraph;

import org.csstudio.swt.xygraph.figures.ToolbarArmedXYGraph;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class XYGraphView extends ViewPart {
	public static final String ID = "net.powermatcher.simulation.gui.xygraph";

	@Override
	public void createPartControl(Composite parent) {
		// use LightweightSystem to create the bridge between SWT and draw2D
		final LightweightSystem lws = new LightweightSystem(new Canvas(parent, SWT.NONE));

		// create the bid graph with a toolbar
		BidTimeSeriesXYGraph bidGraph = new BidTimeSeriesXYGraph();
		ToolbarArmedXYGraph toolbarArmedXYGraph = new ToolbarArmedXYGraph(bidGraph);
		lws.setContents(toolbarArmedXYGraph);
	}

	@Override
	public void setFocus() {
	}
	
	@Override
	public String getTitleToolTip() {
		return "Graph";
	}

}
