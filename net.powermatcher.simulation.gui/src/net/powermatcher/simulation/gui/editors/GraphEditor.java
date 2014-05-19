package net.powermatcher.simulation.gui.editors;

import net.powermatcher.simulation.gui.graph.BidTimeSeriesXYGraph;

import org.csstudio.swt.xygraph.figures.ToolbarArmedXYGraph;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

public class GraphEditor extends EditorPart {
	public static final String ID = GraphEditor.class.getName().toLowerCase();

	public GraphEditor() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		// TODO Auto-generated method stub

	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		// TODO Auto-generated method stub
		setSite(site);
		setInput(input);
		setPartName(getUser());

	}

	private String getUser() {
		// TODO Auto-generated method stub
		return ((GraphViewerInput) getEditorInput()).getName();
	}

	@Override
	public boolean isDirty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void createPartControl(Composite parent) {
		Canvas canvas = new Canvas(parent, SWT.NONE);
		final LightweightSystem lws = new LightweightSystem(canvas);

		BidTimeSeriesXYGraph bidGraph = new BidTimeSeriesXYGraph();
		ToolbarArmedXYGraph toolbarArmedXYGraph = new ToolbarArmedXYGraph(bidGraph);
		lws.setContents(toolbarArmedXYGraph);

		// // TODO Auto-generated method stub
		// //use LightweightSystem to create the bridge between SWT and draw2D
		// final LightweightSystem lws = new LightweightSystem(new
		// Canvas(parent, SWT.NONE));
		//
		//
		// //create a new XY Graph.
		// XYGraph xyGraph = new XYGraph();
		//
		// ToolbarArmedXYGraph toolbarArmedXYGraph = new
		// ToolbarArmedXYGraph(xyGraph);
		//
		// xyGraph.setTitle(getUser());
		// //set it as the content of LightwightSystem
		// lws.setContents(toolbarArmedXYGraph);
		//
		// //create a trace data provider, which will provide the data to the
		// trace.
		// CircularBufferDataProvider traceDataProvider = new
		// CircularBufferDataProvider(false);
		// traceDataProvider.setBufferSize(100);
		// traceDataProvider.setCurrentXDataArray(new double[]{10, 23, 34, 45,
		// 56, 78, 88, 99});
		// traceDataProvider.setCurrentYDataArray(new double[]{11, 44, 55, 45,
		// 88, 98, 52, 23});
		//
		// //create the trace
		// Trace trace = new Trace("Trace1-XY Plot",
		// xyGraph.primaryXAxis, xyGraph.primaryYAxis, traceDataProvider);
		//
		// //set trace property
		// trace.setPointStyle(PointStyle.XCROSS);
		//
		// //add the trace to xyGraph
		// xyGraph.addTrace(trace);

	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public String getTitleToolTip() {
		return "Graph";
	}

}
