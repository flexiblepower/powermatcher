package net.powermatcher.api.data;

import java.util.Arrays;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import net.powermatcher.api.Agent;

/**
 * This immutable data object represents a {@link Bid} with a {@link PricePoint}
 * array to represent the bid curve. This is used by {@link Agent}s that have to
 * create a {@link Bid}, because it is easier to create.
 * 
 * @author FAN
 * @version 2.0
 */
public class PointBid extends Bid implements Iterable<PricePoint> {

	/**
	 * A builder class to create an {@link PointBid} instance.
	 * 
	 * @author FAN
	 * @version 2.0
	 */
	public static final class Builder {

		/**
		 * The {@link MarketBasis} of the cluster.
		 */
		private final MarketBasis marketBasis;

		/**
		 * The number or id of this Bid instance.
		 */
		private int bidNumber;

		/**
		 * The set of {@link PointBid} values that make up the bid curve.
		 */
		private final SortedSet<PricePoint> pricePoints;

		/**
		 * Constructor to create an instance of this class.
		 * 
		 * @param marketBasis
		 *            the {@link MarketBasis} of the cluster.
		 */
		public Builder(final MarketBasis marketBasis) {
			this.marketBasis = marketBasis;
			bidNumber = 0;
			this.pricePoints = new TreeSet<PricePoint>();
		}

		/**
		 * Sets the bidNumber with the specified bidNumber
		 * 
		 * @param bidNumber
		 * @return this instance of the Builder with the set bidNumber
		 */
		public Builder setBidNumber(int bidNumber) {
			this.bidNumber = bidNumber;
			return this;
		}

		/**
		 * Adds the supplied pricePoint the PricePoint array
		 * 
		 * @param pricePoint
		 * @return this instance of the Builder with the array
		 */
		public Builder add(PricePoint pricePoint) {
			pricePoints.add(pricePoint);
			return this;
		}

		/**
		 * Creates a PricePoint with the supplied price and demand. Adds the
		 * point to the PricePoint array.
		 * 
		 * @param pricePoint
		 * @return this instance of the Builder with the array
		 */
		public Builder add(double price, double demand) {
			return add(new PricePoint(marketBasis, price, demand));
		}

		/**
		 * Uses the supplied parameters to create a new PointBid
		 * 
		 * @return The created {@link PointBid}
		 * @throws IllegalArgumentException
		 *             when the marketBasis is null
		 */
		public PointBid build() {
			return new PointBid(marketBasis, bidNumber,
					pricePoints.toArray(new PricePoint[pricePoints.size()]));
		}
	}

	/**
	 * The array of {@link PricePoint}s that make up the bid curve.
	 */
	private final PricePoint[] pricePoints;

	/**
	 * The {@link ArrayBid} representation of this PointBid
	 */
	private transient ArrayBid arrayBid;

	/**
	 * A constructor to create an instance of PointBid.
	 * 
	 * @param marketBasis
	 *            the {@link MarketBasis} of the cluster
	 * @param bidNumber
	 *            the number of this ArrayBid instance
	 * @param pricePoints
	 *            the {@link PointBid} Array that belongs to this bid.
	 */
	public PointBid(MarketBasis marketBasis, int bidNumber,
			PricePoint[] pricePoints) {
		super(marketBasis, bidNumber);
		this.pricePoints = pricePoints;
	}

	/**
	 * A constructor used to create an PointBid, based on a {@link ArrayBid}.
	 * 
	 * @param base
	 *            The {@link ArrayBid} this PointBid will be based on.
	 */
	PointBid(ArrayBid base) {
		super(base.marketBasis, base.bidNumber);
		this.pricePoints = base.calculatePricePoints();
		this.arrayBid = base;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayBid aggregate(Bid other) {
		return toArrayBid().aggregate(other);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Price calculateIntersection(double targetDemand) {
		return toArrayBid().calculateIntersection(targetDemand);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getMaximumDemand() {
		return getFirst().getDemand();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getMinimumDemand() {
		return getLast().getDemand();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ArrayBid toArrayBid() {
		if (arrayBid == null) {
			this.arrayBid = new ArrayBid(this);
		}
		return arrayBid;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public PointBid toPointBid() {
		return this;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double getDemandAt(Price price) {
		if (pricePoints.length == 0) {
			// If there are no pricepoints, all demands are 0
			return 0;
		} else if (price.compareTo(getFirst().getPrice()) <= 0) {
			// If the price is lower than the lowest price, return the maximum
			// demand
			return getMaximumDemand();
		} else if (price.compareTo(getLast().getPrice()) >= 0) {
			// If the price is higher than the highest price, return the minimum
			// demand
			return getMinimumDemand();
		}

		// Now it must be somewhere in between 2 pricepoints
		// First determine which 2 pricepoints

		int ix = 0;
		PricePoint lower = pricePoints[ix++];
		PricePoint higher = pricePoints[ix++];
		while (higher.getPrice().compareTo(price) < 0) {
			lower = higher;
			higher = pricePoints[ix++];
		}

		// Now calculate the demand between the 2 points
		// First the factor (between 0 and 1) of where the price is on the line
		double factor = (price.getPriceValue() - lower.getPrice()
				.getPriceValue())
				/ (higher.getPrice().getPriceValue() - lower.getPrice()
						.getPriceValue());
		// Now calculate the demand
		return (1 - factor) * lower.getDemand() + factor * higher.getDemand();
	}

	/**
	 * @return the first {@link PricePoint} in the bid curve.
	 */
	private PricePoint getFirst() {
		return pricePoints[0];
	}

	/**
	 * @return the last {@link PricePoint} in the bid curve.
	 */
	private PricePoint getLast() {
		return pricePoints[pricePoints.length - 1];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<PricePoint> iterator() {
		return new Iterator<PricePoint>() {
			private int nextIndex;

			/**
			 * {@inheritDoc}
			 */
			@Override
			public PricePoint next() {
				return pricePoints[nextIndex++];
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public boolean hasNext() {
				return nextIndex < pricePoints.length;
			}

			/**
			 * {@inheritDoc}
			 */
			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}

	/**
	 * @return A <code>array[]</code> representation of the {@link PricePoint}
	 *         array.
	 */
	double[] calculateDemandArray() {
		int priceSteps = marketBasis.getPriceSteps();
		double[] newDemand = new double[priceSteps];
		int numPoints = pricePoints.length;
		int i = 0;
		double lastValue = numPoints == 0 ? 0 : getFirst().getDemand();
		for (int p = 0; p < numPoints; p++) {
			PricePoint pricePoint = pricePoints[p];
			int priceStep = pricePoint.getPrice().toPriceStep().getPriceStep();
			int steps = priceStep - i + 1;
			double value = pricePoint.getDemand();
			if (steps > 0) {
				double delta = (value - lastValue) / steps;
				while (i <= priceStep) {
					newDemand[i] = value - (priceStep - i) * delta;
					i += 1;
				}
			} else {
				newDemand[priceStep] = value;
			}
			lastValue = value;
		}
		while (i < priceSteps) {
			newDemand[i++] = lastValue;
		}
		return newDemand;
	}

	/**
	 * @return a copy of pricePoints.
	 */
	public PricePoint[] getPricePoints() {
		return Arrays.copyOf(pricePoints, pricePoints.length);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return 2011 * Arrays.deepHashCode(pricePoints) + 3557 * bidNumber
				+ marketBasis.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		PointBid other = (PointBid) ((obj instanceof PointBid) ? obj : null);
		if (other == null) {
			return false;
		}

		if (this == other) {
			return true;
		}
		return other.bidNumber == this.bidNumber
				&& this.marketBasis.equals(other.marketBasis)
				&& Arrays.equals(other.getPricePoints(), this.getPricePoints());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("PointBid{bidNumber=").append(this.bidNumber);
		PricePoint[] points = getPricePoints();
		/*
		 * Print price points if available, and if the most compact
		 * representation
		 */
		if (points != null
				&& points.length < this.marketBasis.getPriceSteps() / 2) {
			b.append(", PricePoint[]{");
			for (int i = 0; i < points.length; i++) {
				if (i > 0) {
					b.append(',');
				}
				b.append(points[i].toString());
			}
		}
		b.append("}, ");
		b.append(this.marketBasis);
		b.append('}');
		return b.toString();
	}

}
