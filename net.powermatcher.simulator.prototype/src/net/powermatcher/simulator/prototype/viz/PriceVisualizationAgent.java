package net.powermatcher.simulator.prototype.viz;

import javax.swing.WindowConstants;

import net.powermatcher.simulator.prototype.TimeSource;
import net.powermatcher.simulator.prototype.pmcore.Agent;
import net.powermatcher.simulator.prototype.pmcore.Matcher;

public class PriceVisualizationAgent implements Agent {
	private final TimeSource timeSource;
	private Visualization visualization;

	public PriceVisualizationAgent(TimeSource timeSource) {
		this(timeSource, new Visualization());

		visualization.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		visualization.setVisible(true);
	}

	public PriceVisualizationAgent(TimeSource timeSource, Visualization visualization) {
		this.timeSource = timeSource;
		this.visualization = visualization;
	}

	public void setPrice(double price) {
		visualization.addPrice(timeSource.getCurrentTimeMillis(), price);
	}

	public void setMatcher(Matcher matcher) {
	}

	public String getId() {
		return "viz";
	}
}