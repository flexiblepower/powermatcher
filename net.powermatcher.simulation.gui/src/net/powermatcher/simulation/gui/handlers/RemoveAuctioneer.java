package net.powermatcher.simulation.gui.handlers;

import java.util.List;

import net.powermatcher.simulation.configuration.AuctioneerNodeDescriptor;
import net.powermatcher.simulation.configuration.ClusterDescriptor;
import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.gui.Application;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

// TODO is this class being used?
public class RemoveAuctioneer extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		StructuredSelection selection = (StructuredSelection) HandlerUtil.getActiveMenuSelection(event);
		NodeDescriptor node = (NodeDescriptor) selection.getFirstElement();
		if (node == null) {
			return null;
		}

		if (node instanceof AuctioneerNodeDescriptor) {
			ClusterDescriptor cd = getCluster(node.getClusterId());
			List<AuctioneerNodeDescriptor> children = cd.getChildren();
			for (NodeDescriptor nd : children) {
				System.out.println("name:" + nd.getId());
			}
			cd.removeRoot((AuctioneerNodeDescriptor) node);
			if (cd != null) {
				NodeDescriptor parent = getParent(cd, node);
				if (parent != null) {
					parent.removeChild(node);
				}
			}

		} else {
			MessageDialog.openInformation(Application.getInstance().getShell(), "Info",
					"The selection is not a Concentrator node");
		}

		return null;
	}

	private NodeDescriptor getParent(ClusterDescriptor cd, NodeDescriptor cnode) {
		NodeDescriptor root = cd.getRoot();
		return checkChildren(root, cnode);
	}

	private NodeDescriptor checkChildren(NodeDescriptor parent, NodeDescriptor cnode) {
		if (parent.hasChildren()) {
			List<NodeDescriptor> children = parent.getChildren();
			for (NodeDescriptor c : children) {
				System.out.println(c.getId() + ":" + cnode.getId());
				if (c.getId().equalsIgnoreCase(cnode.getId())) {
					System.out.println("found parent");
					return parent;
				}
			}

		} else {
			List<NodeDescriptor> children = parent.getChildren();
			for (NodeDescriptor c : children) {
				checkChildren(c, cnode);
			}
		}
		return null;
	}

	private ClusterDescriptor getCluster(String clusterId) {
		List<ClusterDescriptor> cluster = Application.getInstance().getScenarios().getActiveScenario().getChildren();
		for (ClusterDescriptor cd : cluster) {
			if (cd.getClusterId().equalsIgnoreCase(clusterId)) {
				return cd;
			}
		}

		return null;
	}

}
