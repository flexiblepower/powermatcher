package net.powermatcher.simulation.gui.graph;

import java.util.ArrayList;
import java.util.Collection;
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

public class PriceTimeSeriesXYGraph extends TimeSeriesGraph implements UpdatableGraph, DataSink {
	private long lastTimestamp = -1;
	private final Axis priceAxis;

	private final CircularBufferDataProvider priceData;

	private final BlockingQueue<PriceLogInfo> priceQueue = new LinkedBlockingQueue<PriceLogInfo>();

	public PriceTimeSeriesXYGraph() {
		this.setTitle("Price");

		priceAxis = this.primaryYAxis;
		priceAxis.setTitle("Price [?]");

		priceData = new CircularBufferDataProvider(false);
		priceData.setBufferSize(1000);
		Trace priceTrace = new Trace("Price", getTimeAxis(), priceAxis, priceData);
		// priceTrace.setPointStyle(PointStyle.POINT);
		this.addTrace(priceTrace);
	}

	@Override
	public void handleBidLogInfo(BidLogInfo bidLogInfo) {
	}

	@Override
	public void handlePriceLogInfo(PriceLogInfo priceLogInfo) {
		long now = System.currentTimeMillis();
		if (now != this.lastTimestamp) {
			priceQueue.add(priceLogInfo);
			this.lastTimestamp = now;
		}
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
		Collection<PriceLogInfo> prices = new ArrayList<PriceLogInfo>(1000);
		priceQueue.drainTo(prices);
		for (PriceLogInfo priceInfo : prices) {
			double price = priceInfo.getCurrentPrice();
			priceData.addSample(new Sample(priceInfo.getTimestamp().getTime(), price));

			if (priceAxis.getRange().getUpper() < price) {
				priceAxis.setRange(new Range(0, price + 10));
			}
		}
	}
}
