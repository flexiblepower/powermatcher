package net.powermatcher.simulation.gui.graph;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.log.BidLogInfo;
import net.powermatcher.core.agent.framework.log.PriceLogInfo;
import net.powermatcher.simulation.engine.logging.DataSink;
import net.powermatcher.simulation.gui.Application;
import net.powermatcher.telemetry.model.data.TelemetryData;

import org.csstudio.swt.widgets.datadefinition.ColorMap;
import org.csstudio.swt.widgets.datadefinition.ColorMap.PredefinedColorMap;
import org.csstudio.swt.widgets.figures.IntensityGraphFigure;

public class BidTextureGraph extends IntensityGraphFigure implements UpdatableGraph, DataSink {
	private double[] bidDataArray;

	private final BlockingQueue<BidLogInfo> bidQueue = new LinkedBlockingQueue<BidLogInfo>();

	private long lastTimestamp = -1;

	public BidTextureGraph(int width, int height) {
		this.bidDataArray = new double[width * height];

		this.setDataWidth(width);
		this.setDataHeight(height);
		this.setMin(-100);
		this.setMax(100);
		this.setColorMap(new ColorMap(PredefinedColorMap.ColorSpectrum, true, true));
		this.setDataArray(this.bidDataArray);
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

		procBidLoop: for (BidLogInfo bid : bids) {
			BidInfo bidInfo = bid.getBidInfo();
			if (bidInfo == null) {
				continue procBidLoop;
			}

			double[] tempBidDataArray = new double[this.bidDataArray.length];

			System.arraycopy(this.bidDataArray, this.getDataWidth(), tempBidDataArray, 0, this.bidDataArray.length
					- this.getDataWidth());

			int offset = (this.getDataHeight() - 1) * this.getDataWidth();
			int priceSteps = bid.getMarketBasis().getPriceSteps();
			for (int priceStep = 0; priceStep < priceSteps; priceStep++) {
				tempBidDataArray[offset + priceStep] = bidInfo.getDemand(priceStep);
			}

			this.setDataArray(tempBidDataArray);
			this.bidDataArray = tempBidDataArray;
		}
	}
}
