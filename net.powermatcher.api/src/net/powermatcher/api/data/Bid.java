package net.powermatcher.api.data;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * Bid is an immutable data class specifying a bid curve in either price points
 * or by an demand array representation.
 * </p>
 * 
 * @author FAN
 * @version 1.0
 */
public class Bid {

	/**
	 * The marketBasis for bids and prices.
	 */
	private MarketBasis marketBasis;

	/**
	 * Demand array for each price step in the market basis.
	 */
	private double[] demand;

	/**
	 * Price points in a bid curve .
	 */
	private PricePoint[] pricePoints;

	/**
	 * Holds the bidNumber.
	 */
	private int bidNumber;

	/**
	 * Constructs an instance of this class from the specified other parameter.
	 * 
	 * @param other
	 *            The other (<code>Bid</code>) parameter.
	 * @see #Bid(MarketBasis)
	 * @see #Bid(MarketBasis,double[])
	 * @see #Bid(MarketBasis,PricePoint)
	 * @see #Bid(MarketBasis,PricePoint,PricePoint)
	 * @see #Bid(MarketBasis,PricePoint[])
	 */
	public Bid(final Bid other) {
		this.marketBasis = other.marketBasis;
		this.demand = other.demand;
		this.pricePoints = other.pricePoints;
	}

	/**
	 * Constructs an instance of this class from the specified other parameter,
	 * but assign a new bid number.
	 * 
	 * @param other
	 *            The other (<code>Bid</code>) parameter.
	 * @see #Bid(MarketBasis)
	 * @see #Bid(MarketBasis,double[])
	 * @see #Bid(MarketBasis,PricePoint)
	 * @see #Bid(MarketBasis,PricePoint,PricePoint)
	 * @see #Bid(MarketBasis,PricePoint[])
	 */
	public Bid(final Bid other, final int newBidNumer) {
		this(other);
		this.bidNumber = newBidNumer;
	}

	/**
	 * Constructs an instance of this class from the specified market basis
	 * parameter. An zero bid is created.
	 * 
	 * @param marketBasis
	 *            The market basis (<code>MarketBasis</code>) parameter.
	 * @see #Bid(Bid)
	 * @see #Bid(MarketBasis,double[])
	 * @see #Bid(MarketBasis,PricePoint)
	 * @see #Bid(MarketBasis,PricePoint,PricePoint)
	 * @see #Bid(MarketBasis,PricePoint[])
	 */
	public Bid(final MarketBasis marketBasis) {
		this(marketBasis, new PricePoint(0, 0.0d));
	}

	/**
	 * Constructs an instance of this class from the specified market basis and
	 * demand parameters. The demand array will be cloned.
	 * 
	 * @param marketBasis
	 *            The market basis (<code>MarketBasis</code>) parameter.
	 * @param demand
	 *            The demand (<code>double[]</code>) parameter.
	 * @see #Bid(Bid)
	 * @see #Bid(MarketBasis)
	 * @see #Bid(MarketBasis,PricePoint)
	 * @see #Bid(MarketBasis,PricePoint,PricePoint)
	 * @see #Bid(MarketBasis,PricePoint[])
	 */
	public Bid(final MarketBasis marketBasis, final double[] demand) {
		if (demand == null || marketBasis.getPriceSteps() != demand.length) {
			throw new InvalidParameterException(
					"Missing or incorrect number of demand values for market basis.");
		}
		double lastDemand = demand[0];
		for (int i = 1; i < demand.length; i++) {
			double currentDemand = demand[i];
			if (currentDemand > lastDemand) {
				throw new InvalidParameterException(
						"Bid must be strictly descending.");
			}
			lastDemand = currentDemand;
		}
		this.marketBasis = marketBasis;
		this.demand = demand.clone();
	}

	/**
	 * Constructs an instance of this class from the specified market basis and
	 * price point parameters. The price point will be cloned.
	 * 
	 * @param marketBasis
	 *            The market basis (<code>MarketBasis</code>) parameter.
	 * @param pricePoint
	 *            The price point (<code>PricePoint</code>) parameter.
	 * @see #Bid(Bid)
	 * @see #Bid(MarketBasis)
	 * @see #Bid(MarketBasis,double[])
	 * @see #Bid(MarketBasis,PricePoint,PricePoint)
	 * @see #Bid(MarketBasis,PricePoint[])
	 */
	public Bid(final MarketBasis marketBasis, final PricePoint pricePoint) {
		this.marketBasis = marketBasis;
		this.pricePoints = new PricePoint[] { new PricePoint(pricePoint) };
	}

	/**
	 * Constructs an instance of this class from the specified market basis,
	 * price point1 and price point2 parameters. The price points will be
	 * cloned.
	 * 
	 * @param marketBasis
	 *            The market basis (<code>MarketBasis</code>) parameter.
	 * @param pricePoint1
	 *            The price point1 (<code>PricePoint</code>) parameter.
	 * @param pricePoint2
	 *            The price point2 (<code>PricePoint</code>) parameter.
	 * @see #Bid(Bid)
	 * @see #Bid(MarketBasis)
	 * @see #Bid(MarketBasis,double[])
	 * @see #Bid(MarketBasis,PricePoint)
	 * @see #Bid(MarketBasis,PricePoint[])
	 */
	public Bid(final MarketBasis marketBasis, final PricePoint pricePoint1,
			final PricePoint pricePoint2) {
		this(marketBasis, new PricePoint[] { pricePoint1, pricePoint2 });
	}

	/**
	 * Constructs an instance of this class from the specified market basis and
	 * price points parameters. The demand array will not be cloned, so should
	 * not be updated beyond this call.
	 * 
	 * @param marketBasis
	 *            The market basis (<code>MarketBasis</code>) parameter.
	 * @param bidNumber
	 *            The bid number
	 * @param pricePoints
	 *            The price points (<code>PricePoint[]</code>) parameter.
	 * @see #Bid(Bid)
	 * @see #Bid(MarketBasis)
	 * @see #Bid(MarketBasis,double[])
	 * @see #Bid(MarketBasis,PricePoint)
	 * @see #Bid(MarketBasis,PricePoint,PricePoint)
	 */
	private Bid(final MarketBasis marketBasis, final int bidNumber,
			final PricePoint[] pricePoints) {
		this.marketBasis = marketBasis;
		this.bidNumber = bidNumber;
		this.pricePoints = clone(pricePoints);
	}

	/**
	 * Constructs an instance of this class from the specified market basis and
	 * price points parameters. The price points array will be cloned.
	 * 
	 * @param marketBasis
	 *            The market basis (<code>MarketBasis</code>) parameter.
	 * @param pricePoints
	 *            The price points (<code>PricePoint[]</code>) parameter.
	 * @see #Bid(Bid)
	 * @see #Bid(MarketBasis)
	 * @see #Bid(MarketBasis,double[])
	 * @see #Bid(MarketBasis,PricePoint)
	 * @see #Bid(MarketBasis,PricePoint,PricePoint)
	 */
	public Bid(final MarketBasis marketBasis, final PricePoint[] pricePoints) {
		if (pricePoints == null || pricePoints.length < 0) {
			throw new InvalidParameterException(
					"Missing or incorrect number of price points.");
		}
		if (pricePoints.length > 0) {
			double lastDemand = pricePoints[0].getDemand();
			double lastNormalizedPrice = pricePoints[0].getNormalizedPrice();
			for (int i = 1; i < pricePoints.length; i++) {
				double currentDemand = pricePoints[i].getDemand();
				double currentNormalizedPrice = pricePoints[i]
						.getNormalizedPrice();
				if (currentNormalizedPrice < lastNormalizedPrice) {
					throw new InvalidParameterException(
							"Price must be strictly ascending.");
				}
				if (currentDemand > lastDemand) {
					throw new InvalidParameterException(
							"Bid must be strictly descending.");
				}
				lastDemand = currentDemand;
			}
		}
		this.marketBasis = marketBasis;
		this.pricePoints = clone(pricePoints);
	}

	/**
	 * Constructs an instance of this class from the specified market basis, bid
	 * number and demand parameters. The demand array will not be cloned, so
	 * should not be updated beyond this call.
	 * 
	 * @param marketBasis
	 *            The market basis (<code>MarketBasis</code>) parameter.
	 * @param bidNumber
	 *            The bid number
	 * @param demand
	 *            The demand (<code>double[]</code>) parameter.
	 * @see #Bid()
	 * @see #Bid(Bid)
	 * @see #Bid(MarketBasis)
	 * @see #Bid(MarketBasis,PricePoint)
	 * @see #Bid(MarketBasis,PricePoint,PricePoint)
	 * @see #Bid(MarketBasis,PricePoint[])
	 */
	private Bid(MarketBasis marketBasis, final int bidNumber, double[] demand) {
		this.marketBasis = marketBasis;
		this.bidNumber = bidNumber;
		this.demand = demand;
	}

	/**
	 * Aggregate with the specified other parameter.
	 * 
	 * @param other
	 *            The other (<code>Bid</code>) parameter.
	 * @return A copy of this bid with the other bid aggregated into it.
	 */
	public Bid aggregate(final Bid other) {
		double[] otherDemand = other.toMarketBasis(this.marketBasis)
				.getUnclonedDemand();
		double[] aggregatedDemand = this.getDemand();
		for (int i = 0; i < aggregatedDemand.length; i++) {
			aggregatedDemand[i] += otherDemand[i];
		}
		return new Bid(this.marketBasis, this.bidNumber, aggregatedDemand);
	}

	/**
	 * Calculate intersection with the specified target demand and return the
	 * Price result.
	 * 
	 * @param targetDemand
	 *            The target demand (<code>double</code>) parameter.
	 * @return Results of the intersection (<code>Price</code>) value.
	 */
	// TODO The Cyclomatic Complexity of this method "calculateIntersection" is
	// 12 which is greater than 10 authorized.
	public Price calculateIntersection(final double targetDemand) {
		double[] unclonedDemand = getUnclonedDemand();
		int leftBound = 0;
		int rightBound = unclonedDemand.length - 1;
		int middle = rightBound / 2;
		while (leftBound < middle) {
			if (unclonedDemand[middle] > targetDemand) {
				leftBound = middle;
			} else {
				rightBound = middle;
			}
			middle = (leftBound + rightBound) / 2;
		}
	
		/*
		 * Find the point where the demand falls below the target
		 */
		while (rightBound < unclonedDemand.length
				&& unclonedDemand[rightBound] >= targetDemand) {
			rightBound += 1;
		}
	
		/*
		 * The index of the point which is just above the intersection is now
		 * stored in the variable 'middle'. This means that middle + 1 is the
		 * index of the point that lies just under the intersection. That means
		 * that the exact intersection is between middle and middle+1, hence a
		 * weighted interpolation is needed.
		 * 
		 * Pricing for boundary cases: 1) If the curve is a flat line, if the
		 * demand is positive the price will become the maximum price, or if the
		 * demand is zero or negative, the price will become 0. 2) If the curve
		 * is a single step, the price of the stepping point (the left or the
		 * right boundary).
		 */
		int priceStep;
	
		// TODO Refactor this code to not nest more than 3
		// if/for/while/switch/try statements.
		if (leftBound > 0 && rightBound < unclonedDemand.length) {
			/* Interpolate */
			double interpolation = ((unclonedDemand[leftBound] - targetDemand) / (unclonedDemand[leftBound] - unclonedDemand[rightBound]))
					* (rightBound - leftBound);
			priceStep = leftBound + (int) Math.ceil(interpolation);
		} else {
			if (leftBound == 0 && rightBound == unclonedDemand.length) {
				/* Curve is flat line or single step at leftBound */
				if (unclonedDemand[leftBound] != 0) {
					/*
					 * Curve is flat line for non-zero demand or single step at
					 * leftBound
					 */
					if (unclonedDemand[leftBound + 1] == 0) {
						/* Curve is a single step at leftBound */
						priceStep = leftBound + 1;
					} else {
						/* Curve is flat line for non-zero demand */
						priceStep = rightBound - 1;
					}
				} else {
					/* Curve is flat line for zero demand 0 */
					priceStep = this.marketBasis.toPriceStep(0);
				}
			} else {
				/* Curve is a single step at leftBound */
				priceStep = unclonedDemand[leftBound] <= targetDemand ? leftBound
						: leftBound + 1;
			}
		}
		double intersectionPrice = this.marketBasis.toPrice(this.marketBasis
				.boundPriceStep(priceStep));
		return new Price(this.marketBasis, intersectionPrice);
	}

	/**
	 * Subtract the other bid curve from this bid curve. Subtract is the inverse
	 * of aggregate. The other bid does not have to be based on the same market
	 * basis.
	 * 
	 * @param other
	 *            The other (<code>Bid</code>) parameter.
	 * @return A copy of this bid with the other bid subtracted from it.
	 */
	public Bid subtract(final Bid other) {
		double[] otherDemand = other.toMarketBasis(this.marketBasis)
				.getUnclonedDemand();
		double[] newDemand = this.getDemand();
		for (int i = 0; i < newDemand.length; i++) {
			newDemand[i] -= otherDemand[i];
		}
		return new Bid(this.marketBasis, this.bidNumber, newDemand);
	}

	/**
	 * Convert this bid to another market basis.
	 * 
	 * @param newMarketBasis
	 *            The new market basis (<code>MarketBasis</code>) parameter.
	 * @return A copy of this bid converted to the new market basis.
	 * @see #getMarketBasis()
	 */
	public Bid toMarketBasis(final MarketBasis newMarketBasis) {
		if (this.marketBasis.equals(newMarketBasis)) {
			return this;
		} else {
			assert this.marketBasis.getCommodity().equals(
					newMarketBasis.getCommodity());
			assert this.marketBasis.getCurrency().equals(
					newMarketBasis.getCurrency());
			if (this.pricePoints == null) {
				double[] oldDemand = getUnclonedDemand();
				double[] newDemand = new double[newMarketBasis.getPriceSteps()];
				for (int i = 0; i < newDemand.length; i++) {
					double newPrice = this.marketBasis
							.boundPrice(newMarketBasis.toPrice(i));
					int oldPriceStep = this.marketBasis.toPriceStep(newPrice);
					newDemand[i] = oldDemand[oldPriceStep];
				}
				return new Bid(newMarketBasis, this.bidNumber, newDemand);
			} else {
				PricePoint[] newPricePoints = new PricePoint[this.pricePoints.length];
				for (int i = 0; i < newPricePoints.length; i++) {
					PricePoint oldPricePoint = this.pricePoints[i];
					double price = this.marketBasis.toPrice(this.marketBasis
							.toPriceStep(oldPricePoint.getNormalizedPrice()));
					newPricePoints[i] = new PricePoint(
							newMarketBasis.toNormalizedPrice(price),
							oldPricePoint.getDemand());
				}
				return new Bid(newMarketBasis, this.bidNumber, newPricePoints);
			}
		}
	}

	/**
	 * Transpose the bid curve by adding an offset to the demand.
	 * 
	 * @param offset
	 *            The offset (<code>double</code>) parameter.
	 */
	public Bid transpose(final double offset) {
		double[] newDemand = this.getDemand();
		for (int i = 0; i < newDemand.length; i++) {
			newDemand[i] += offset;
		}
		return new Bid(this.marketBasis, this.bidNumber, newDemand);
	}

	/**
	 * Create a new PricePoint corresponding to the demand value at priceSte.
	 * 
	 * @param priceStep
	 *            The price step (<code>int</code>) parameter.
	 * @return The new (<code>PricePoint</code>).
	 */
	private PricePoint newPoint(int priceStep) {
		return new PricePoint(this.marketBasis.toNormalizedPrice(priceStep),
				this.demand[priceStep]);
	}

	/**
	 * Gets the bid number (int) value.
	 * 
	 * @return The bid number (<code>int</code>) value.
	 */
	public int getBidNumber() {
		return this.bidNumber;
	}

	/**
	 * Gets the cloned demand (double[]) value.
	 * 
	 * @return The cloned demand (<code>double[]</code>) value.
	 * @see #getDemand(double)
	 * @see #getDemand(int)
	 * @see #getMaximumDemand()
	 * @see #getMinimumDemand()
	 */
	public double[] getDemand() {
		return getUnclonedDemand().clone();
	}

	/**
	 * Gets the uncloned demand (double[]) value.
	 * 
	 * @return The uncloned demand (<code>double[]</code>) value.
	 * @see #getDemand(double)
	 * @see #getDemand(int)
	 * @see #getMaximumDemand()
	 * @see #getMinimumDemand()
	 */
	private double[] getUnclonedDemand() {
		// TODO Refactor this code to not nest more than 3
		// if/for/while/switch/try statements
		if (this.demand == null && this.pricePoints != null) {
			int priceSteps = this.marketBasis.getPriceSteps();
			double[] newDemand = new double[priceSteps];
			int numPoints = this.pricePoints.length;
			int i = 0;
			double lastValue = numPoints == 0 ? 0 : this.pricePoints[0]
					.getDemand();
			for (int p = 0; p < numPoints; p++) {
				PricePoint pricePoint = this.pricePoints[p];
				int priceStep = this.marketBasis.toPriceStep(pricePoint
						.getNormalizedPrice());
				priceStep = this.marketBasis.boundPriceStep(priceStep);
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
			this.demand = newDemand;
		}
		return this.demand;
	}

	/**
	 * Get demand with the specified price parameter and return the double
	 * result.
	 * 
	 * @param price
	 *            The price (<code>double</code>) parameter.
	 * @return Results of the get demand (<code>double</code>) value.
	 * @see #getDemand()
	 * @see #getDemand(int)
	 * @see #getMaximumDemand()
	 * @see #getMinimumDemand()
	 */
	public double getDemand(final double price) {
		return getDemand(this.marketBasis.toPriceStep(price));
	}

	/**
	 * Get demand with the specified price step parameter and return the double
	 * result.
	 * 
	 * @param priceStep
	 *            The price step (<code>int</code>) parameter.
	 * @return Results of the get demand (<code>double</code>) value.
	 * @see #getDemand()
	 * @see #getDemand(double)
	 * @see #getMaximumDemand()
	 * @see #getMinimumDemand()
	 */
	public double getDemand(final int priceStep) {
		double[] unclonedDemand = getUnclonedDemand();
		return unclonedDemand[this.marketBasis.boundPriceStep(priceStep)];
	}

	/**
	 * Gets the market basis value.
	 * 
	 * @return The market basis (<code>MarketBasis</code>) value.
	 * @see #toMarketBasis(MarketBasis)
	 */
	public MarketBasis getMarketBasis() {
		return this.marketBasis;
	}

	/**
	 * Gets the maximum demand (double) value.
	 * 
	 * @return The maximum demand (<code>double</code>) value.
	 */
	public double getMaximumDemand() {
		double maxDemand = 0;
		if (this.pricePoints != null && this.pricePoints.length > 0) {
			maxDemand = this.pricePoints[0].getDemand();
		} else if (this.demand != null && this.demand.length > 0) {
			maxDemand = this.demand[0];
		}
		return maxDemand;
	}

	/**
	 * Gets the minimum demand (double) value.
	 * 
	 * @return The minimum demand (<code>double</code>) value.
	 */
	public double getMinimumDemand() {
		double minDemand = 0;
		if (this.pricePoints != null && this.pricePoints.length > 0) {
			minDemand = this.pricePoints[this.pricePoints.length - 1]
					.getDemand();
		} else if (this.demand != null && this.demand.length > 0) {
			minDemand = this.demand[this.demand.length - 1];
		}
		return minDemand;
	}

	/**
	 * Gets the cloned price points (PricePoint[]) value.
	 * 
	 * @return The cloned price points (<code>PricePoint[]</code>) value, or
	 *         null if the price points have not been calculated or set.
	 * @see #getCalculatedPricePoints()
	 */
	public PricePoint[] getPricePoints() {
		return clone(this.pricePoints);
	}

	/**
	 * Gets the cloned calculated price points (PricePoint[]) value. The price
	 * points will be calculated from the demand array, if necessary.
	 * 
	 * @return The cloned price points (<code>PricePoint[]</code>) value, or
	 *         null if no price points or demand array has been set.
	 * @see #getPricePoints()
	 */
	// TODO The Cyclomatic Complexity of this method "getCalculatedPricePoints"
	// is 18 which is greater than 10 authorized.
	public PricePoint[] getCalculatedPricePoints() {
		PricePoint[] calculatedPricePoints = null;
		if (this.pricePoints != null) {
			calculatedPricePoints = this.pricePoints.clone();
		} else if (this.demand != null) {
			int priceSteps = this.marketBasis.getPriceSteps();
			assert this.demand.length == priceSteps;
			List<PricePoint> points = new ArrayList<PricePoint>(priceSteps);
			/*
			 * Flag to indicate if the last price point is the start of a flat
			 * segment.
			 */
			boolean flatStart = false;
			/*
			 * Flag to indicate if the last price point is the end point of a
			 * flat segment.
			 */
			boolean flatEnd = false;
			int i = 0;
			while (i < priceSteps - 1) {
				/*
				 * Search for the last price step in the flat segment, if any.
				 * At the end of this loop delta is the difference for the next
				 * demand after the flat segment.
				 */
				PricePoint flatEndPoint = null;
				double delta = 0.0d;
				while (i < priceSteps - 1
						&& (delta = this.demand[i] - this.demand[i + 1]) == 0) {
					i += 1;
					flatEnd = true;
				}
				/*
				 * Add a point at i for the following two cases: 1) If not at
				 * the end of the demand array, add the next point for i and
				 * remember that this point is the end of a flat segment. 2) Add
				 * a final point if past the end of the demand array, but the
				 * previous point was not the beginning of a flat segment.
				 */
				if (i < priceSteps - 1 || !flatStart) {
					flatEndPoint = newPoint(i);
					points.add(flatEndPoint);
					flatStart = false;
				}
				i += 1;

				// TODO Refactor this code to not nest more than 3
				// if/for/while/switch/try statements.
				/*
				 * If not at the end of the demand array, check if the following
				 * segment is a step or an inclining segment.
				 */
				if (i < priceSteps - 1) {
					// TODO Test for floating point equality. Not just ==
					if (this.demand[i] - this.demand[i + 1] == delta) {
						/*
						 * Here i is in a constantly inclining or declining
						 * segment. Search for the last price step in the
						 * segment.
						 */
						while (i < priceSteps - 1
								&& this.demand[i] - this.demand[i + 1] == delta) {
							i += 1;
						}
						/*
						 * If not at the end of the demand array, add the end
						 * point for the segment.
						 */
						if (i < priceSteps - 1) {
							points.add(newPoint(i));
							i += 1;
						}
					} else if (flatEnd && flatEndPoint != null) {
						/*
						 * If i is not in a constantly inclining or declining
						 * segment, and the previous segment was flat, then move
						 * the end point of the flat segment one price step
						 * forward to convert it to a straight step.
						 * 
						 * This is to preserve the semantics of the straight
						 * step when converting between point and vector
						 * representation and back.
						 */
						flatEndPoint.setNormalizedPrice(this.marketBasis
								.toNormalizedPrice(i));
					}
				}

				/*
				 * Add a point at i for the following two cases: 1) Add a point
				 * for the start of the next flat segment and loop. 2) Add a
				 * final point if the last step of the demand array the end of
				 * an inclining or declining segment.
				 */
				if (i == priceSteps - 1
						|| (i < priceSteps - 1 && this.demand[i]
								- this.demand[i + 1] == 0)) {
					points.add(newPoint(i));
					flatStart = true;
				}
			}
			calculatedPricePoints = new PricePoint[points.size()];
			points.toArray(calculatedPricePoints);
		}
		return calculatedPricePoints;
	}

	/**
	 * Get scale factor with the specified max value parameter and return the
	 * double result.
	 * 
	 * @param maxValue
	 *            The max value (<code>int</code>) parameter.
	 * @return Results of the get scale factor (<code>double</code>) value.
	 */
	public double getScaleFactor(final int maxValue) {
		return Math.max(getMaximumDemand(), -getMinimumDemand()) / maxValue;
	}

    /**
     * Compares the provided object to this instance of the class according to
     * the equals rules.
     * 
     * @param obj
     *          The object to compare with.
     * @return true if this equals the object, false otherwise.
     */
    @Override
    public boolean equals(final Object obj) {
        Bid other = (Bid) ((obj instanceof Bid) ? obj : null);
        if (other == null)
            return false;

        if (this == other)
            return true;

        return other.bidNumber == this.bidNumber && this.marketBasis.equals(other.marketBasis)
                && Arrays.equals(other.getUnclonedDemand(), this.getUnclonedDemand());
    }

	/**
	 * Hash code and return the int result.
	 * 
	 * @return Results of the hash code (<code>int</code>) value.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = prime + this.bidNumber;
		result = prime * result + Arrays.hashCode(getUnclonedDemand());
		result = prime
				* result
				+ ((this.marketBasis == null) ? 0 : this.marketBasis.hashCode());
		return result;
	}

	/**
	 * Returns the string value.
	 * 
	 * @return The string (<code>String</code>) value.
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("Bid{bidNumber=").append(this.bidNumber);
		PricePoint[] points = this.pricePoints;
		/*
		 * Print price points if available, and if the most compact
		 * representation
		 */
		// TODO Refactor this code to not nest more than 3
		// if/for/while/switch/try statements.
		if (points != null
				&& points.length < this.marketBasis.getPriceSteps() / 2) {
			b.append(", PricePoint[]{");
			for (int i = 0; i < points.length; i++) {
				if (i > 0) {
					b.append(',');
				}
				PricePoint pricePoint = points[i];
				int priceStep = this.marketBasis.toPriceStep(pricePoint
						.getNormalizedPrice());
				b.append('(').append(
						MarketBasis.PRICE_FORMAT.format(this.marketBasis
								.toPrice(priceStep)));
				b.append(",")
						.append(MarketBasis.DEMAND_FORMAT.format(pricePoint
								.getDemand()));
				b.append(')');
			}
			b.append("}, ");
		} else {
			double[] unclonedDemand = getUnclonedDemand();
			if (unclonedDemand != null) {
				b.append(", demand[]{");
				for (int i = 0; i < unclonedDemand.length; i++) {
					if (i > 0) {
						b.append(',');
					}
					b.append(MarketBasis.DEMAND_FORMAT
							.format(unclonedDemand[i]));
				}
				b.append("}, ");
			}
		}
		b.append(this.marketBasis);
		b.append('}');
		return b.toString();
	}

	/**
	 * Create a deep clone of the price point array.
	 * 
	 * @param unclonedPricePoints
	 *            The price point array to be cloned, or null.
	 * @return The cloned price point array, or null if unclonedPricePoints is
	 *         null.
	 */
	private static PricePoint[] clone(PricePoint[] unclonedPricePoints) {
		if (unclonedPricePoints == null) {
			// TODO this should return new PricePoint[0], not null.
			// getCalculatedPricePoints might will have to be refactored

			return null;
		}
		PricePoint[] pricePoints = new PricePoint[unclonedPricePoints.length];
		for (int i = 0; i < pricePoints.length; i++) {
			pricePoints[i] = new PricePoint(unclonedPricePoints[i]);
		}
		return pricePoints;
	}

}
