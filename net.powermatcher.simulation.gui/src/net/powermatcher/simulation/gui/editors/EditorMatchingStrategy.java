package net.powermatcher.simulation.gui.editors;


import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorMatchingStrategy;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PartInitException;

public class EditorMatchingStrategy implements IEditorMatchingStrategy {

	@Override
	public boolean matches(IEditorReference editorRef, IEditorInput input) {
		// TODO Auto-generated method stub
		if (!(input instanceof AgentEditorInput))
		      return false;

		    try {
		      return editorRef.getEditorInput().equals(input);
		    } catch (PartInitException e) {
		      return false;
		    }
		  
	}

}
