package net.powermatcher.simulation.gui.handlers;

import net.powermatcher.simulation.configuration.ScenarioDescriptor;
import net.powermatcher.simulation.gui.Application;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class SetActive extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		//System.out.println("setactive called");
		StructuredSelection selection = (StructuredSelection) HandlerUtil.getActiveMenuSelection(event);
		ScenarioDescriptor node = null; 
		if(selection.getFirstElement() instanceof ScenarioDescriptor){
			node = (ScenarioDescriptor) selection.getFirstElement();
		}
		
		if (node == null) {
			return null;
		} else{
			Application.getInstance().getScenarios().setActiveScenario(node);
		}

		
		return null;
	}

}
