package net.powermatcher.simulation.gui.operations;

import java.util.HashMap;

import net.powermatcher.core.object.config.IdentifiableObjectConfiguration;
import net.powermatcher.simulation.configuration.AuctioneerNodeDescriptor;
import net.powermatcher.simulation.configuration.ClusterDescriptor;
import net.powermatcher.simulation.configuration.ScenarioDescriptor;
import net.powermatcher.simulation.gui.ComponentCountManager;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class AddClusterOperation extends AbstractOperation {
	private final ScenarioDescriptor scenarioDescriptor;

	private AuctioneerNodeDescriptor auctioneer;
	private ClusterDescriptor clusterDescriptor;

	public AddClusterOperation(ScenarioDescriptor scenarioDescriptor) {
		super("Add cluster operation");

		this.scenarioDescriptor = scenarioDescriptor;
	}

	@Override
	public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		this.clusterDescriptor = new ClusterDescriptor();
		this.clusterDescriptor.setClusterId("Cluster-"
				+ ComponentCountManager.getInstance().getNextCount(ClusterDescriptor.class));

		this.auctioneer = new AuctioneerNodeDescriptor();
		this.auctioneer.setFactoryPid("net.powermatcher.core.agent.auctioneer.Auctioneer");
		this.auctioneer.setConfiguration(createConfiguration(this.clusterDescriptor, "Auctioneer-"
				+ ComponentCountManager.getInstance().getNextCount(AuctioneerNodeDescriptor.class)));
		this.clusterDescriptor.setRoot(this.auctioneer);

		this.scenarioDescriptor.addChild(this.clusterDescriptor);

		return Status.OK_STATUS;
	}

	@Override
	public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		this.scenarioDescriptor.addChild(this.clusterDescriptor);
		return Status.OK_STATUS;
	}

	@Override
	public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		this.scenarioDescriptor.removeChild(this.clusterDescriptor);
		return Status.OK_STATUS;
	}

	private static HashMap<String, Object> createConfiguration(ClusterDescriptor clusterDescriptor, String nodeId) {
		HashMap<String, Object> properties = new HashMap<String, Object>();
		properties.put(IdentifiableObjectConfiguration.CLUSTER_ID_PROPERTY, clusterDescriptor.getClusterId());
		properties.put(IdentifiableObjectConfiguration.ID_PROPERTY, nodeId);
		return properties;
	}
}
