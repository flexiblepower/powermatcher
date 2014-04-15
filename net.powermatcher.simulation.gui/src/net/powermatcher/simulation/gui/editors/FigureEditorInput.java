package net.powermatcher.simulation.gui.editors;

import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class FigureEditorInput implements IEditorInput {
	public static enum GraphType { 
		PRICE, BID, BID_TEXTURE
	}
	private String name;
	private IFigure figure;
	private GraphType type;

	public FigureEditorInput(String name, IFigure figure, GraphType type) {
		Assert.isNotNull(name);
		Assert.isNotNull(figure);

		this.name = name;
		this.figure = figure;
		this.type = type;
	}

	@Override
	public String getName() {
		return this.name;
	}

	public IFigure getFigure() {
		return figure;
	}

	public GraphType getGraphType(){
		return this.type;
	}
	@Override
	@SuppressWarnings("rawtypes")
	public Object getAdapter(Class adapter) {
		return null;
	}

	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return null;
	}
}
