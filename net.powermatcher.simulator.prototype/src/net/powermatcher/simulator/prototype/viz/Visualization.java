package net.powermatcher.simulator.prototype.viz;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import net.powermatcher.simulator.prototype.dependencyengine.SimulationControl;

import com.sun.tools.visualvm.charts.ChartFactory;
import com.sun.tools.visualvm.charts.SimpleXYChartDescriptor;
import com.sun.tools.visualvm.charts.SimpleXYChartSupport;

public class Visualization extends JFrame {
	private static final long serialVersionUID = 1L;

	private SimpleXYChartSupport priceChart;
	private JLabel rateLabel = new JLabel();

	private long lastRateLabelUpdate = System.currentTimeMillis();
	private long priceUpdatesSinceLabelUpdate = 0;

	private SimpleXYChartSupport pricesPerSecondChart;

	private SimulationControl continuationControl;

	public Visualization() {
		this(null);
	}

	public Visualization(SimulationControl continuationControl) {
		super("price");

		this.continuationControl = continuationControl;

		SimpleXYChartDescriptor priceChartDescriptor = SimpleXYChartDescriptor.decimal(0, true, 500);
		priceChartDescriptor.addLineItems("Price");
		priceChart = ChartFactory.createSimpleXYChart(priceChartDescriptor);

		priceChartDescriptor = SimpleXYChartDescriptor.decimal(0, true, 500);
		priceChartDescriptor.addLineItems("Prices / second (1/1000)");
		pricesPerSecondChart = ChartFactory.createSimpleXYChart(priceChartDescriptor);

		rateLabel = new JLabel(createRateLabelText(0));
		JPanel rateLabelPanel = new JPanel(new BorderLayout());
		rateLabelPanel.add(rateLabel, BorderLayout.SOUTH);
		rateLabel.setBorder(BorderFactory.createLoweredBevelBorder());

		JComponent chart = pricesPerSecondChart.getChart();
		chart.setPreferredSize(new Dimension(1024, 200));
		rateLabelPanel.add(chart, BorderLayout.CENTER);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(priceChart.getChart(), BorderLayout.CENTER);
		panel.add(rateLabelPanel, BorderLayout.SOUTH);

		if (continuationControl != null) {
			JToolBar continuationToolBar = new JToolBar();
			panel.add(continuationToolBar, BorderLayout.NORTH);

			addButton(continuationToolBar, "start", new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Visualization.this.continuationControl.start();
				}
			});
			addButton(continuationToolBar, "pause", new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Visualization.this.continuationControl.pause();
				}
			});
			addButton(continuationToolBar, "step", new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Visualization.this.continuationControl.step();
				}
			});
			addButton(continuationToolBar, "stop", new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Visualization.this.continuationControl.stop();
				}
			});
		}

		this.getContentPane().add(panel);
		this.setPreferredSize(new Dimension(1024, 768));
		this.pack();
	}

	private void addButton(JToolBar continuationToolBar, String label, ActionListener actionListener) {
		JButton startButton = new JButton(label);
		startButton.addActionListener(actionListener);
		continuationToolBar.add(startButton);
	}

	public void addPrice(final long time, final double price) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				priceChart.addValues(time, new long[] { (long) (price * 100) });

				priceUpdatesSinceLabelUpdate++;
				if (lastRateLabelUpdate < System.currentTimeMillis() - 1000) {
					double pricesPerSecond = priceUpdatesSinceLabelUpdate
							/ ((System.currentTimeMillis() - lastRateLabelUpdate) / 1000.0);
					rateLabel.setText(createRateLabelText(pricesPerSecond));

					pricesPerSecondChart.addValues(System.currentTimeMillis(),
							new long[] { (long) (pricesPerSecond * 1000) });

					lastRateLabelUpdate = System.currentTimeMillis();
					priceUpdatesSinceLabelUpdate = 0;
				}
			}
		});
	}

	private String createRateLabelText(double pricesPerSecond) {
		return "Prices / second : " + String.format("%.3f", pricesPerSecond);
	}
}
