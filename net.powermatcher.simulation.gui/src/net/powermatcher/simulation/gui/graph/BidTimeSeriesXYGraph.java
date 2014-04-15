package net.powermatcher.simulation.gui.graph;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.powermatcher.core.agent.framework.log.BidLogInfo;
import net.powermatcher.core.agent.framework.log.PriceLogInfo;
import net.powermatcher.simulation.engine.logging.DataSink;
import net.powermatcher.simulation.gui.Application;
import net.powermatcher.telemetry.model.data.TelemetryData;

import org.csstudio.swt.xygraph.dataprovider.CircularBufferDataProvider;
import org.csstudio.swt.xygraph.dataprovider.Sample;
import org.csstudio.swt.xygraph.figures.Axis;
import org.csstudio.swt.xygraph.figures.Trace;
import org.csstudio.swt.xygraph.linearscale.Range;

public class BidTimeSeriesXYGraph extends TimeSeriesGraph implements UpdatableGraph, DataSink {
	private final CircularBufferDataProvider allocationData;

	private final BlockingQueue<BidLogInfo> bidQueue = new LinkedBlockingQueue<BidLogInfo>();

	private long lastTimestamp = -1;
	private final CircularBufferDataProvider maxDemandData;
	private final CircularBufferDataProvider minDemandData;

	private final Axis powerAxis;

	public BidTimeSeriesXYGraph() {
		this.setTitle("Power");

		this.powerAxis = this.primaryYAxis;
		this.powerAxis.setTitle("Power [W]");

		this.maxDemandData = new CircularBufferDataProvider(false);
		this.maxDemandData.setBufferSize(1000);
		Trace maxDemandTrace = new Trace("Max. demand", getTimeAxis(), this.powerAxis, this.maxDemandData);
		// maxDemandTrace.setPointStyle(PointStyle.POINT);
		this.addTrace(maxDemandTrace);

		this.minDemandData = new CircularBufferDataProvider(false);
		this.minDemandData.setBufferSize(1000);
		Trace minDemandTrace = new Trace("Min. demand", getTimeAxis(), this.powerAxis, this.minDemandData);
		// minDemandTrace.setPointStyle(PointStyle.POINT);
		this.addTrace(minDemandTrace);

		this.allocationData = new CircularBufferDataProvider(false);
		this.allocationData.setBufferSize(1000);
		Trace allocationTrace = new Trace("Allocation", getTimeAxis(), this.powerAxis, this.allocationData);
		// allocationTrace.setPointStyle(PointStyle.POINT);
		this.addTrace(allocationTrace);
	}

	@Override
	public void handleBidLogInfo(BidLogInfo bidLogInfo) {
		long now = System.currentTimeMillis();
		if (now != this.lastTimestamp) {
			this.bidQueue.add(bidLogInfo);
			this.lastTimestamp = now;
		}
	}

	@Override
	public void handlePriceLogInfo(PriceLogInfo priceLogInfo) {
	}

	@Override
	public void processTelemetryData(TelemetryData telemetryData) {
	}

	@Override
	public void simulationCycleBegins(long timestamp) {
	}

	@Override
	public void simulationCycleFinishes(long timestamp) {
	}

	@Override
	public void simulationFinished() {
		Application.getInstance().getGraphUpdater().remove(this);
	}

	@Override
	public void simulationStarts(long timestamp) {
		Application.getInstance().getGraphUpdater().add(this);
	}

	@Override
	public void updateGraph() {
		Collection<BidLogInfo> bids = new LinkedList<BidLogInfo>();
		this.bidQueue.drainTo(bids);

		// int bidInterval = Math.max(1, bids.size() / 10);

		// int i = 0;
		for (BidLogInfo bid : bids) {
			// if (i++ % bidInterval != 0) {
			// continue;
			// }

			double maxDemand = bid.getMaximumDemand();
			double minDemand = bid.getMinimumDemand();

			long time = bid.getTimestamp().getTime();
			double effectiveDemand = bid.getEffectiveDemand();

			if (minDemand != maxDemand || minDemand != effectiveDemand) {
				this.minDemandData.addSample(new Sample(time, minDemand));
				this.maxDemandData.addSample(new Sample(time, maxDemand));
			}

			this.allocationData.addSample(new Sample(time, effectiveDemand));

			if (minDemand < this.powerAxis.getRange().getLower()) {
				this.powerAxis.setRange(new Range(minDemand - 10, this.powerAxis.getRange().getUpper()));
			}

			if (this.powerAxis.getRange().getUpper() < maxDemand) {
				this.powerAxis.setRange(new Range(this.powerAxis.getRange().getLower(), maxDemand + 10));
			}
		}
	}
}
