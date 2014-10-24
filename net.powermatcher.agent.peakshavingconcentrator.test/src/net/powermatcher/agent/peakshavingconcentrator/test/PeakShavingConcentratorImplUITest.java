package net.powermatcher.agent.peakshavingconcentrator.test;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.WindowConstants;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.MarketBasis;
import net.powermatcher.core.agent.framework.data.PriceInfo;
import net.powermatcher.core.configurable.BaseConfiguration;
import net.powermatcher.core.scheduler.SchedulerAdapterFactory;
import net.powermatcher.core.scheduler.service.TimeServicable;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;


public class PeakShavingConcentratorImplUITest implements Runnable {
	private static final double[] BID = new double[] { 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1 };
	private static final MarketBasis MARKET_BASIS = new MarketBasis("electricity", "EUR", BID.length, 0,
			BID.length - 1, 1, 0);

	public static void main(String[] args) throws Exception {
		PeakShavingConcentratorImplUITest test = new PeakShavingConcentratorImplUITest();
		test.initialize();
		test.start();
	}

	private ScheduledExecutorService scheduler;
	private TimeServicable timeSource;
	private MockPeakShavingConcentratorImpl concentrator;

	private XYSeries totalFlowSeries;
	private XYSeries allocationSeries;
	private XYSeries priceSeries;
	private XYSeries uncontrolledFlowOutputSeries;

	private long time;
	private volatile boolean running;

	private JButton startButton;
	private JButton pauseButton;
	private JButton resetButton;
	private JToggleButton manualFlowToggleButton;
	private JSlider uncontrolledFlowSlider;

	public void initialize() {
		SchedulerAdapterFactory schedulerAdapterFactory = SchedulerAdapterFactory.getSchedulerAdapterFactory(); 
		scheduler = schedulerAdapterFactory.getScheduler();
		timeSource = schedulerAdapterFactory.getTimeSource();
		concentrator = createConcentrator(MARKET_BASIS);

		concentrator.setFlowConstraints(10, -10);
		concentrator.updateBidInfo("1", new BidInfo(MARKET_BASIS, BID));
		concentrator.updatePriceInfo(new PriceInfo(MARKET_BASIS, 0));

		totalFlowSeries = new XYSeries("total flow");
		allocationSeries = new XYSeries("allocation");
		uncontrolledFlowOutputSeries = new XYSeries("uncontrolled flow");
		priceSeries = new XYSeries("price");

		createUI();
	}

	private void createUI() {
		final XYSeriesCollection seriesCollection = new XYSeriesCollection();
		seriesCollection.addSeries(totalFlowSeries);
		seriesCollection.addSeries(uncontrolledFlowOutputSeries);
		seriesCollection.addSeries(allocationSeries);
		seriesCollection.addSeries(priceSeries);

		ChartPanel panel = new ChartPanel(createChart(seriesCollection));

		startButton = new JButton("Start");
		pauseButton = new JButton("Pause");
		resetButton = new JButton("Reset");
		manualFlowToggleButton = new JToggleButton("Set flow manually");

		pauseButton.setEnabled(false);
		resetButton.setEnabled(false);
		manualFlowToggleButton.setSelected(false);

		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				start();
			}
		});

		pauseButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				pause();
			}
		});

		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reset();
			}
		});

		JMenuBar menuBar = new JMenuBar();
		menuBar.add(startButton);
		menuBar.add(pauseButton);
		menuBar.add(resetButton);
		menuBar.add(manualFlowToggleButton);

		JPanel toggleChartPanel = new JPanel();
		for (int i = 0; i < seriesCollection.getSeriesCount(); i++) {
			final XYSeries series = seriesCollection.getSeries(i);
			final JToggleButton toggleButton = new JToggleButton("" + series.getKey());

			toggleChartPanel.add(toggleButton);
			toggleButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					if (toggleButton.isSelected()) {
						seriesCollection.removeSeries(series);
					} else {
						seriesCollection.addSeries(series);
					}
				}
			});
		}

		uncontrolledFlowSlider = new JSlider(-20 * 1000, 20 * 1000);
		uncontrolledFlowSlider.setName("uncontrolled flow");
		uncontrolledFlowSlider.setOrientation(JSlider.VERTICAL);

		JFrame frame = new JFrame("Couperus Peak Shaving Concentrator Test");
		frame.setLayout(new BorderLayout());
		frame.add(menuBar, BorderLayout.NORTH);
		frame.add(uncontrolledFlowSlider, BorderLayout.WEST);
		frame.add(panel, BorderLayout.CENTER);
		frame.add(toggleChartPanel, BorderLayout.SOUTH);

		frame.setSize(new Dimension(1200, 800));
		frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		frame.setVisible(true);

		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}

	private void start() {
		time = 0;
		running = true;

		new Thread(this).start();

		startButton.setEnabled(false);
		pauseButton.setEnabled(true);
		resetButton.setEnabled(true);
	}

	private void pause() {
		running = !running;

		if (running) {
			new Thread(this).start();
			pauseButton.setText("Pause");
		} else {
			pauseButton.setText("Continue");
		}
	}

	private void reset() {
		running = false;

		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		concentrator.setFlowConstraints(10, -10);
		concentrator.updateBidInfo("1", new BidInfo(MARKET_BASIS, BID));
		concentrator.updatePriceInfo(new PriceInfo(MARKET_BASIS, 0));
		concentrator.setMeasuredFlow(0);

		synchronized (this) {
			totalFlowSeries.clear();
			allocationSeries.clear();
			uncontrolledFlowOutputSeries.clear();
			priceSeries.clear();
		}

		startButton.setEnabled(true);
		pauseButton.setEnabled(false);
		resetButton.setEnabled(false);
		pauseButton.setText("Pause");
	}

	public void run() {
		if (running == false) {
			return;
		}

		while (running) {
			double uncontrolledFlow = manualFlowToggleButton.isSelected() ? uncontrolledFlowSlider.getValue() / 1000d
					: Math.sin(Math.toRadians(((double) time) / 10)) * 10;

			// set the new measured flow
			concentrator.setMeasuredFlow(uncontrolledFlow + concentrator.getAllocation());

			// ensure direct feedback by taking the new allocation into account
			concentrator.setMeasuredFlow(uncontrolledFlow + concentrator.getAllocation());

			synchronized (this) {
				totalFlowSeries.add(time, uncontrolledFlow + concentrator.getAllocation());
				allocationSeries.add(time, concentrator.getAllocation());
				priceSeries.add(time, concentrator.getPropagatedPrice().getCurrentPrice());
				uncontrolledFlowOutputSeries.add(time, concentrator.getUncontrolledFlow());
			}

			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			time++;
		}
	}

	private MockPeakShavingConcentratorImpl createConcentrator(MarketBasis marketBasis) {
		Properties properties = new Properties();
		properties.setProperty("id", "a");

		MockPeakShavingConcentratorImpl mock = new MockPeakShavingConcentratorImpl();
		mock.setConfiguration(new BaseConfiguration(properties));
		mock.bind(timeSource);
		mock.bind(scheduler);
		mock.updateMarketBasis(marketBasis);

		return mock;
	}

	private JFreeChart createChart(XYSeriesCollection seriesCollection) {
		DateAxis dateAxis = new DateAxis("time");
		NumberAxis numberAxis = new NumberAxis("value");

		XYPlot plot = new XYPlot(seriesCollection, dateAxis, numberAxis, new XYLineAndShapeRenderer(true, false));

		return new JFreeChart("peak shaving concentrator", plot);
	}
}
