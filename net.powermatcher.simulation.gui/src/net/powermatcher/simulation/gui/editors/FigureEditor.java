package net.powermatcher.simulation.gui.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;

public class FigureEditor extends EditorPart {
	public static final String ID = FigureEditor.class.getName();

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(input.getName());
	}

	@Override
	public void createPartControl(Composite parent) {
		Canvas canvas = new Canvas(parent, SWT.NONE);

		LightweightSystem lws = new LightweightSystem(canvas);
		lws.setContents(((FigureEditorInput) getEditorInput()).getFigure());

		int operations = DND.DROP_MOVE | DND.DROP_COPY | DND.DROP_DEFAULT;
		DropTarget dropTarget = new DropTarget(canvas, operations);
		dropTarget.setTransfer(new Transfer[] { TextTransfer.getInstance() });

		dropTarget.addDropListener(new DropTargetListener() {
			@Override
			public void dropAccept(DropTargetEvent e) {
				// TODO Auto-generated method stub
				System.out.println("dropAccept");
			}

			@Override
			public void drop(DropTargetEvent e) {
				// TODO Auto-generated method stub
				System.out.println("drop");
			}

			@Override
			public void dragOver(DropTargetEvent e) {
				// TODO Auto-generated method stub
				System.out.println("dragOver");
			}

			@Override
			public void dragOperationChanged(DropTargetEvent e) {
				// TODO Auto-generated method stub
				System.out.println("dragOperationChanged");
			}

			@Override
			public void dragLeave(DropTargetEvent e) {
				// TODO Auto-generated method stub
				System.out.println("dragLeave");
			}

			@Override
			public void dragEnter(DropTargetEvent e) {
				// TODO Auto-generated method stub
				System.out.println("dragEnter");
			}
		});
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void setFocus() {
	}
}
