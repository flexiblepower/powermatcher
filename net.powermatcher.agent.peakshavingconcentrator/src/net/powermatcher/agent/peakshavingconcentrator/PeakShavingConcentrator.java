package net.powermatcher.agent.peakshavingconcentrator;

import java.util.Arrays;

import net.powermatcher.core.agent.concentrator.framework.AbstractPeakShavingConcentrator;
import net.powermatcher.core.agent.framework.data.BidInfo;
import net.powermatcher.core.agent.framework.data.PriceInfo;

/**
 * Implementation of a peak shaving algorithm which decouples the propagated
 * price from the received price to ensure that a ceiling and floor in power
 * flow isn't exceeded. It's clipping behavior is reflected in the propagated
 * bids. The peak shaving algorithm is able to take power flow from uncontrolled
 * sources into account by using flow measurements (and deducing the
 * uncontrolled flow from the allocation based on the aggregated bid curve and
 * the propagated price).
 */
public class PeakShavingConcentrator extends AbstractPeakShavingConcentrator {

	/* 
	 * The event handling methods 
	 */

	@Override
	protected synchronized BidInfo transformAggregatedBid(final BidInfo newAggregatedBid) {
		// if the given bid is null, then there is nothing to transform, so we
		// let the next framework handle the null bid
		if (newAggregatedBid == null) {
			return null;
		}

		// Copy the aggregated bid to transform into a new aggregated bid
		BidInfo newAggregatedBidOut = new BidInfo(newAggregatedBid);

		// add the uncontrolled flow
		if (!Double.isNaN(this.getUncontrolledFlow())) {
			newAggregatedBidOut = newAggregatedBidOut.transpose(this.getUncontrolledFlow());
		}

		// clip above the ceiling and blow the floor
		newAggregatedBidOut = this.clipAbove(newAggregatedBidOut, this.ceiling);
		newAggregatedBidOut = this.clipBelow(newAggregatedBidOut, this.floor);

		// remove the uncontrolled flow again
		if (!Double.isNaN(this.getUncontrolledFlow())) {
			newAggregatedBidOut = newAggregatedBidOut.transpose(-this.getUncontrolledFlow());
		}

		// remember what the incoming and outgoing aggregated bids were.
		this.aggregatedBidIn = newAggregatedBid;
		this.aggregatedBidOut = newAggregatedBidOut;

		return this.aggregatedBidOut;
	}

	@Override
	protected synchronized PriceInfo adjustPrice(final PriceInfo newPrice) {
		// if the given price is null, the price can't be adjusted, so we let
		// the framework handle the null price
		if (newPrice == null) {
			return null;
		}

		// we can only adjust if we know the aggregated bid curve
		if (this.aggregatedBidIn == null) {
			this.priceIn = newPrice;
			return this.priceOut = newPrice;
		}

		// transform prices to indices
		int priceInIndex = newPrice.getMarketBasis().toPriceStep(newPrice.getCurrentPrice());
		int priceOutIndex = priceInIndex;

		// create a copy of the aggregated bid to calculate the demand function
		// and add the uncontrolled flow to the demand function
		BidInfo demandFunction = new BidInfo(this.aggregatedBidIn);
		double uncontrolledFlow = this.getUncontrolledFlow();
		if (!Double.isNaN(uncontrolledFlow)) {
			demandFunction = demandFunction.transpose(uncontrolledFlow);
		}

		// determine the expected allocation
		double allocation = demandFunction.getDemand(newPrice.getCurrentPrice());

		// Adjust the price so that the allocation is within flow
		// constraints (taking uncontrolled flow into account)
		if (!Double.isNaN(this.ceiling) && allocation > this.ceiling) {
			priceOutIndex = this.findFirstIndexOfUnclippedRegion(demandFunction.getDemand(), this.ceiling);

			// if there is no unclipped region we use the lowest price
			if (priceOutIndex == -1) {
				priceOutIndex = demandFunction.getMarketBasis().getPriceSteps() - 1;
			}
		} else if (allocation < this.floor) {
			priceOutIndex = this.findLastIndexOfUnclippedRegion(demandFunction.getDemand(), this.floor);

			// if there is no unclipped region we use the highest price
			if (priceOutIndex == -1) {
				priceOutIndex = 0;
			}
		}

		// set the new price
		this.priceIn = newPrice;
		return this.priceOut = new PriceInfo(newPrice.getMarketBasis(), newPrice.getMarketBasis()
				.toPrice(priceOutIndex));
	}

	
	/* 
	 * The utility methods 
	 */

	/**
	 * Clip a bid such that no power value in the bid exceeds the given ceiling.
	 * Any value in the resulting bid will have resulted from the given bid
	 * (i.e. no new power level values will have been introduced).
	 * 
	 * @param bid
	 *            The bid to clip.
	 * @param ceiling
	 *            The ceiling to clip the bid to.
	 * @return The clipped bid.
	 */
	private BidInfo clipAbove(final BidInfo bid, final double ceiling) {
		double[] demand = bid.getDemand();

		// find start of unclipped region
		int start = this.findFirstIndexOfUnclippedRegion(demand, ceiling);

		// if there is no unclipped region we use the last (lowest value)
		if (start == -1) {
			Arrays.fill(demand, demand[demand.length - 1]);
		} else {
			// replace part above ceiling with first point in unclipped region
			double firstUnclippedPoint = demand[start];
			for (int i = 0; i < start; i++) {
				demand[i] = firstUnclippedPoint;
			}
		}
		return new BidInfo(bid.getMarketBasis(), demand);
	}

	/**
	 * Clip a bid such that no power value in the bid exceeds the given floor.
	 * Any value in the resulting bid will have resulted from the given bid
	 * (i.e. no new power level values will have been introduced).
	 * 
	 * @param bid
	 *            The bid to clip.
	 * @param floor
	 *            The floor to clip the bid to.
	 * @return The clipped bid.
	 */
	private BidInfo clipBelow(final BidInfo bid, final double floor) {
		double[] demand = bid.getDemand();

		// find end of unclipped region
		int end = this.findLastIndexOfUnclippedRegion(demand, floor);

		if (end == -1) {
			// if there is no unclipped region we use the first (highest value)
			Arrays.fill(demand, demand[0]);
		} else {
			// replace part below floor with last point in unclipped region
			double lastUnclippedPoint = demand[end];
			for (int i = end + 1; i < demand.length; i++) {
				demand[i] = lastUnclippedPoint;
			}
		}
		return new BidInfo(bid.getMarketBasis(), demand);
	}

	/**
	 * Finds the first index in a demand function for which the allocation
	 * doesn't exceed the given ceiling. Starting at the lowest price (index),
	 * the first index of the region which won't be clipped is the first value
	 * in the demand function which is lower than or equal to the ceiling.
	 * 
	 * @param demandFunction
	 *            The demand function as an array of power flow values, where
	 *            positive flow is demand.
	 * @param floor
	 *            The ceiling which defines the maximum value the unclipped
	 *            region has.
	 * @return The first index of the unclipped region or -1 if there is no
	 *         region which isn't below the ceiling.
	 */
	private int findFirstIndexOfUnclippedRegion(final double[] demandFunction, final double ceiling) {
		for (int i = 0; i < demandFunction.length; i++) {
			if (demandFunction[i] <= ceiling) {
				return i;
			}
		}

		return -1;
	}

	/**
	 * Finds the first index in a demand function for which the allocation
	 * doesn't exceed the given floor; the last index of the region which won't
	 * be clipped is the last value in the demand function which is greater than
	 * or equal to the ceiling.
	 * 
	 * @param demandFunction
	 *            The demand function as an array of power flow values, where
	 *            positive flow is demand.
	 * @param floor
	 *            The floor which defines the minimum value the unclipped region
	 *            has.
	 * @return The last index of the unclipped region or -1 if there is no
	 *         region which isn't above the floor.
	 */
	private int findLastIndexOfUnclippedRegion(final double[] demandFunction, final double floor) {
		for (int i = demandFunction.length - 1; i >= 0; i--) {
			if (demandFunction[i] >= floor) {
				return i;
			}
		}

		return -1;
	}
}
