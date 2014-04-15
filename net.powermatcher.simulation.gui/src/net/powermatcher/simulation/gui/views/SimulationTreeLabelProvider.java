package net.powermatcher.simulation.gui.views;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.powermatcher.simulation.configuration.AuctioneerNodeDescriptor;
import net.powermatcher.simulation.configuration.ClusterDescriptor;
import net.powermatcher.simulation.configuration.ConcentratorNodeDescriptor;
import net.powermatcher.simulation.configuration.NodeDescriptor;
import net.powermatcher.simulation.configuration.ScenarioDescriptor;
import net.powermatcher.simulation.gui.ScenarioContainer;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

public class SimulationTreeLabelProvider extends LabelProvider {
	private Map<String, Image> imageCache = new HashMap<String, Image>();

	@Override
	public String getText(Object inputElement) {
		String label = null;

		if (inputElement instanceof ScenarioContainer) {
			label = "Root";
		} else if (inputElement instanceof ScenarioDescriptor) {
			label = ((ScenarioDescriptor) inputElement).getScenarioId();
		} else if (inputElement instanceof ClusterDescriptor) {
			label = ((ClusterDescriptor) inputElement).getClusterId();
		} else if (inputElement instanceof NodeDescriptor) {
			label = ((NodeDescriptor) inputElement).getId();
		}

		return label != null ? label : super.getText(inputElement);
	}

	@Override
	public Image getImage(Object element) {
		String filename = null;

		if (element instanceof ScenarioDescriptor) {
			if (((ScenarioDescriptor) element).isActive()) {
				filename = "/icons/page_lightning.png";
			} else {
				filename = "/icons/page.png";
			}
		} else if (element instanceof ClusterDescriptor) {
			filename = "/icons/chart_organisation.png";
		} else if (element instanceof AuctioneerNodeDescriptor) {
			filename = "/icons/coins.png";
		} else if (element instanceof ConcentratorNodeDescriptor) {
			filename = "/icons/arrow_join.png";
		} else {
			filename = "/icons/brick.png";
		}

		return loadImage(filename);
	}

	private Image loadImage(String filename) {
		Image image = this.imageCache.get(filename);

		if (image != null) {
			return image;
		}

		Display display = PlatformUI.getWorkbench().getDisplay();
		InputStream stream = this.getClass().getResourceAsStream(filename);

		if (stream == null) {
			return null;
		}

		try {
			image = new Image(display, stream);
			this.imageCache.put(filename, image);
			return image;
		} catch (SWTException ex) {
			return null;
		} finally {
			try {
				stream.close();
			} catch (IOException ex) {
			}
		}
	}

}
