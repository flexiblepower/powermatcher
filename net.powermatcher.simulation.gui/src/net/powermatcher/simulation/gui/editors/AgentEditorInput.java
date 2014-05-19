package net.powermatcher.simulation.gui.editors;

import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.telemetry.metadata.TelemetryMetaData;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.osgi.service.metatype.ObjectClassDefinition;

public class AgentEditorInput implements IEditorInput {

	private ObjectClassDefinition configurationDefinition;
	private NodeDescriptor node;
	private TelemetryMetaData telemetryMetaData;

	public AgentEditorInput(NodeDescriptor node, ObjectClassDefinition configurationDefinition, TelemetryMetaData telemetryData) {
		this.configurationDefinition = configurationDefinition;
		this.node = node;
		this.telemetryMetaData = telemetryData;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return node.getId();
	}

	@Override
	public IPersistableElement getPersistable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getToolTipText() {
		return "Agent editor";
	}

	public ObjectClassDefinition getConfigurationDefinition() {
		return configurationDefinition;
	}

	public TelemetryMetaData getTelemetryMetaData() {
		return this.telemetryMetaData;
	}

	public NodeDescriptor getNodeDescriptor() {
		return this.node;
	}
	

}
