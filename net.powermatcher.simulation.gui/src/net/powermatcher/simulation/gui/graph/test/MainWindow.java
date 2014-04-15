package net.powermatcher.simulation.gui.graph.test;

import java.util.Date;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.agent.framework.log.BidLogInfo;
import net.powermatcher.core.agent.framework.log.LogListenerService;
import net.powermatcher.core.agent.framework.log.PriceLogInfo;
import net.powermatcher.simulation.gui.graph.BidTextureGraph;
import net.powermatcher.simulation.gui.graph.BidTimeSeriesXYGraph;
import net.powermatcher.simulation.gui.graph.GraphUpdater;
import net.powermatcher.simulation.gui.graph.PriceTimeSeriesXYGraph;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LightweightSystem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class MainWindow implements LogListenerService {
	protected static Shell shell;

	private BidTimeSeriesXYGraph bidGraph;
	private PriceTimeSeriesXYGraph priceGraph;
	private BidTextureGraph bidTextureGraph;

	public static void main(String[] args) {
		final MainWindow mainWindow = new MainWindow();
		mainWindow.open();

		final double price[] = { 50 };
		new PriceGenerator(price, mainWindow).start();
		new BidGenerator(price, mainWindow).start();
	}

	public void activate() {
		open();
	}

	public void deactivate() {
		close();
	}

	/**
	 * Open the window.
	 */
	public void open() {
		new Thread() {
			public void run() {
				Display display = Display.getDefault();
				createContents();
				createCustomContents();
				shell.open();
				shell.layout();

				new GraphUpdater(priceGraph, bidGraph, bidTextureGraph).start(display);

				while (!shell.isDisposed()) {
					if (!display.readAndDispatch()) {
						display.sleep();
					}
				}
			}
		}.start();
	}

	public void close() {
		shell.close();
		shell.dispose();
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shell = new Shell();
		shell.setSize(800, 600);
		shell.setText("SWT Application");
	}

	private void createCustomContents() {
		synchronized (MainWindow.this) {
			GridLayout layout = new GridLayout();
			layout.numColumns = 3;
			shell.setLayout(layout);

			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);

			priceGraph = new PriceTimeSeriesXYGraph();
			addToGrid(gd, priceGraph);

			bidGraph = new BidTimeSeriesXYGraph();
			addToGrid(gd, bidGraph);

			bidTextureGraph = new BidTextureGraph(100, 200);
			addToGrid(gd, bidTextureGraph);
		}
	}

	private void addToGrid(GridData gd, IFigure figure) {
		Canvas Canvas = new Canvas(shell, SWT.NONE);
		Canvas.setLayoutData(gd);

		LightweightSystem lws = new LightweightSystem(Canvas);
		lws.setContents(figure);
	}

	@Override
	public void handleBidLogInfo(BidLogInfo bidLogInfo) {
		if (bidGraph != null) {
			bidGraph.handleBidLogInfo(bidLogInfo);
		}

		if (bidTextureGraph != null) {
			bidTextureGraph.handleBidLogInfo(bidLogInfo);
		}
	}

	@Override
	public void handlePriceLogInfo(PriceLogInfo priceLogInfo) {
		if (priceGraph != null) {
			priceGraph.handlePriceLogInfo(priceLogInfo);
		}
	}

	private static final class BidGenerator extends Thread {
		private final MainWindow mainWindow;
		private final double[] price;
		private MarketBasis marketBasis = new MarketBasis("", "", 100, 0, 99, 1, 1);
		private BidInfo bidInfo = new BidInfo(marketBasis);
	
		private BidGenerator(double[] price, MainWindow mainWindow) {
			this.mainWindow = mainWindow;
			this.price = price;
		}
	
		public void run() {
			while (shell == null || shell.isDisposed() == false) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
	
				double[] demand = bidInfo.getDemand();
				double max = demand[0] + Math.random() * 20 - 10;
				demand[0] = max = Math.round(constrain(max, 0, 100));
	
				double min = max - 100 - Math.random() * 20 - 10;
				demand[demand.length - 1] = min = Math.round(constrain(min, -100, 0));
	
				int maxPricePoint = (int) (Math.random() * 10);
				int minPricePoint = demand.length - (int) (Math.random() * 10);
	
				double distanceToMin = max - min;
	
				for (int i = 0; i < maxPricePoint; i++) {
					demand[i] = max;
				}
	
				double averageStep, step;
				for (int i = maxPricePoint + 1; i < minPricePoint; i++) {
					averageStep = distanceToMin / (minPricePoint - i);
					step = Math.random() * averageStep * 10;
					distanceToMin -= step;
					demand[i] = Math.round(constrain(demand[i - 1] - step, min, max));
				}
	
				for (int i = minPricePoint; i < demand.length; i++) {
					demand[i] = min;
				}
	
				bidInfo = new BidInfo(marketBasis, demand);
	
				mainWindow.handleBidLogInfo(new BidLogInfo("clusterId", "agentId", BidLogInfo.MATCHER_LOG_QUALIFIER,
						new Date(), marketBasis, price[0], bidInfo.getDemand(price[0]), bidInfo.getMinimumDemand(),
						bidInfo.getMaximumDemand(), bidInfo));
			}
		}
	
		private double constrain(double max, double high, double low) {
			return max > low ? low : max < high ? high : max;
		}
	}

	private static final class PriceGenerator extends Thread {
		private final double[] price;
		private final MainWindow mainWindow;
		private MarketBasis marketBasis = new MarketBasis("", "", 100, 0, 99, 1, 1);
	
		private PriceGenerator(double[] price, MainWindow mainWindow) {
			this.price = price;
			this.mainWindow = mainWindow;
		}
	
		public void run() {
			while (shell == null || shell.isDisposed() == false) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
	
				double range = marketBasis.getMaximumPrice() - marketBasis.getMinimumPrice();
				double variation = range * .05;
				price[0] += Math.random() * variation - variation / 2;
				price[0] = price[0] > marketBasis.getMaximumPrice() ? marketBasis.getMaximumPrice()
						: price[0] < marketBasis.getMinimumPrice() ? marketBasis.getMinimumPrice() : price[0];
	
				mainWindow.handlePriceLogInfo(new PriceLogInfo("clusterId", "agentId",
						PriceLogInfo.MATCHER_LOG_QUALIFIER, new Date(), new PriceInfo(marketBasis, price[0])));
			}
		}
	}
}
