package net.powermatcher.simulation.gui.handlers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import net.powermatcher.simulation.configuration.DataDescriptor;
import net.powermatcher.simulation.configuration.DataDescriptor.DataType;
import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.gui.Application;
import net.powermatcher.simulation.gui.editors.FigureEditor;
import net.powermatcher.simulation.gui.editors.FigureEditorInput;
import net.powermatcher.simulation.gui.editors.FigureEditorInput.GraphType;
import net.powermatcher.simulation.gui.graph.BidTextureGraph;
import net.powermatcher.simulation.logging.sink.Filter;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

public class ShowBidTextureGraph extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		StructuredSelection selection = (StructuredSelection) HandlerUtil.getActiveMenuSelection(event);
		NodeDescriptor node = (NodeDescriptor) selection.getFirstElement();
		if (node == null) {
			return null;
		} else {
			IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
			IEditorReference[] references = page.getEditorReferences();
			for (IEditorReference ref : references) {
				try {

					IEditorInput editorInput = ref.getEditorInput();
					if (editorInput instanceof FigureEditorInput && node.getId().equals(editorInput.getName())
							&& ((FigureEditorInput) editorInput).getGraphType() == GraphType.BID_TEXTURE) {
						// ref.getEditor(true);
						page.bringToTop(ref.getPart(true));
						return null;
					}
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		BidTextureGraph graph = new BidTextureGraph(1000, 100);
		// TODO graph.setTitle("Bid at " + id);

		Filter filter = new Filter(graph, new DataDescriptor(node.getId(), node.getClusterId(), DataType.BID));

		Application application = Application.getInstance();
		application.getBroker().addDataSink(filter);

		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		FigureEditorInput input = new FigureEditorInput(node.getId(), graph, GraphType.BID_TEXTURE);

		try {
			page.openEditor(input, FigureEditor.ID);
		} catch (PartInitException e) {
			Writer result = new StringWriter();
			e.printStackTrace(new PrintWriter(result));

			MessageDialog.openWarning(Application.getInstance().getShell(), "Can't load graph",
					"The graph could not be opened:\n" + e.getMessage() + "\n" + result.toString());
		}

		return null;
	}
}