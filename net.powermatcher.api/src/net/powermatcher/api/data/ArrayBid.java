package net.powermatcher.api.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArrayBid extends Bid {
	public static final class Builder {
		private final MarketBasis marketBasis;
		private int bidNumber;
		private int nextIndex;
		private final double[] demandArray;
		
		public Builder(MarketBasis marketBasis) {
			this.marketBasis = marketBasis;
			bidNumber = 0;
			nextIndex = 0;
			demandArray = new double[marketBasis.getPriceSteps()];
		}
		
		private void checkIndex(int ix) {
			if(ix >= demandArray.length) {
				throw new ArrayIndexOutOfBoundsException();
			}
		}
		
		public Builder bidNumber(int bidNumber) {
			this.bidNumber = bidNumber;
			return this;
		}
		
		public Builder demand(double demand) {
			checkIndex(nextIndex);
			if(nextIndex > 0) {
				if(demand > demandArray[nextIndex - 1]) {
					throw new IllegalArgumentException("The demand should always be descending");
				}
			}
			demandArray[nextIndex++] = demand;
			return this;
		}
		
		public Builder until(int priceStep) {
			if(nextIndex == 0) {
				throw new IllegalStateException("Start with a demand that can be extended until this value");
			}
			double demand = demandArray[nextIndex-1];
			while(nextIndex < priceStep) {
				demandArray[nextIndex++] = demand;
			}
			return this;
		}
		
		public ArrayBid build() {
			// Make sure the whole array is filled
			until(demandArray.length);
			return new ArrayBid(marketBasis, bidNumber, demandArray);
		}
	}
	
	private static void checkDescending(double[] demandArray) {
		double last = Double.POSITIVE_INFINITY;
		for(double demand : demandArray) {
			if(demand > last){
				throw new IllegalArgumentException("The demandArray must be descending");
			}
			last = demand;
		}
	}

	private final double[] demandArray;
	
	private transient PointBid pointBid;
	
	public ArrayBid(MarketBasis marketBasis, int bidNumber, double[] demandArray) {
		super(marketBasis, bidNumber);
		if(demandArray.length != marketBasis.getPriceSteps()) {
			throw new IllegalArgumentException("Length of the demandArray is not equals to the number of price steps");
		}
		checkDescending(demandArray);
		this.demandArray = Arrays.copyOf(demandArray, demandArray.length);
	}
	
	ArrayBid(PointBid base) {
		super(base.getMarketBasis(), base.getBidNumber());
		this.demandArray = base.calculateDemandArray();
		this.pointBid = base;
   	}
	
	@Override
	public Bid aggregate(Bid other) {
		if(!other.marketBasis.equals(marketBasis)) {
			throw new IllegalArgumentException("These 2 bids are not compatible");
		}
		ArrayBid otherBid = other.toArrayBid();
		
		double[] aggregatedDemand = otherBid.getDemand();
        for (int i = 0; i < aggregatedDemand.length; i++) {
            aggregatedDemand[i] += demandArray[i];
        }
        return new ArrayBid(this.marketBasis, 0, aggregatedDemand);
	}

	@Override
	public PriceUpdate calculateIntersection(final double targetDemand) {
        int leftBound = 0;
        int rightBound = demandArray.length - 1;
        int middle = rightBound / 2;
        while (leftBound < middle) {
            if (demandArray[middle] > targetDemand) {
                leftBound = middle;
            } else {
                rightBound = middle;
            }
            middle = (leftBound + rightBound) / 2;
        }

        /*
         * Find the point where the demand falls below the target
         */
        while (rightBound < demandArray.length && demandArray[rightBound] >= targetDemand) {
            rightBound += 1;
        }

        /*
         * The index of the point which is just above the intersection is now stored in the variable 'middle'. This
         * means that middle + 1 is the index of the point that lies just under the intersection. That means that the
         * exact intersection is between middle and middle+1, hence a weighted interpolation is needed.
         * 
         * Pricing for boundary cases: 1) If the curve is a flat line, if the demand is positive the price will become
         * the maximum price, or if the demand is zero or negative, the price will become 0. 2) If the curve is a single
         * step, the price of the stepping point (the left or the right boundary).
         */
        int priceStep;

        // TODO Refactor this code to not nest more than 3
        // if/for/while/switch/try statements.
        if (leftBound > 0 && rightBound < demandArray.length) {
            /* Interpolate */
            double interpolation = ((demandArray[leftBound] - targetDemand) / (demandArray[leftBound] - demandArray[rightBound]))
                    * (rightBound - leftBound);
            priceStep = leftBound + (int) Math.ceil(interpolation);
        } else {
            if (leftBound == 0 && rightBound == demandArray.length) {
                /* Curve is flat line or single step at leftBound */
                if (demandArray[leftBound] != 0) {
                    /*
                     * Curve is flat line for non-zero demand or single step at leftBound
                     */
                    if (demandArray[leftBound + 1] == 0) {
                        /* Curve is a single step at leftBound */
                        priceStep = leftBound + 1;
                    } else {
                        /* Curve is flat line for non-zero demand */
                        priceStep = rightBound - 1;
                    }
                } else {
                    /* Curve is flat line for zero demand 0 */
                    priceStep = new Price(marketBasis, 0).toPriceStep().getPriceStep();
                }
            } else {
                /* Curve is a single step at leftBound */
                priceStep = demandArray[leftBound] <= targetDemand ? leftBound : leftBound + 1;
            }
        }
        
        return new PriceUpdate(new PriceStep(marketBasis, priceStep).toPrice(), this.bidNumber);
	}

	@Override
	public double getMaximumDemand() {
		return demandArray[0];
	}

	@Override
	public double getMinimumDemand() {
		return demandArray[demandArray.length - 1];
	}
	
	@Override
	public ArrayBid toArrayBid() {
		return this;
	}
	
	public PointBid toPointBid() {
		if(pointBid == null) {
			pointBid = new PointBid(this);
		}
		return pointBid;
	};
	
	public double[] getDemand() {
		return Arrays.copyOf(demandArray, demandArray.length);
	}
	
	public double getDemandAt(PriceStep priceStep) {
		if(!priceStep.getMarketBasis().equals(marketBasis)) {
			throw new IllegalArgumentException("The marketbasis of the pricestep does not equal this market basis");
		}
		return demandArray[priceStep.getPriceStep()];
	}
	
	PricePoint[] calculatePricePoints() {
		int priceSteps = this.marketBasis.getPriceSteps();
        List<PricePoint> points = new ArrayList<PricePoint>(priceSteps);
        /*
         * Flag to indicate if the last price point is the start of a flat segment.
         */
        boolean flatStart = false;
        /*
         * Flag to indicate if the last price point is the end point of a flat segment.
         */
        boolean flatEnd = false;
        int i = 0;
        while (i < priceSteps - 1) {
            /*
             * Search for the last price step in the flat segment, if any. At the end of this loop delta is the
             * difference for the next demand after the flat segment.
             */
            PricePoint flatEndPoint = null;
            double delta = 0.0d;
            while (i < priceSteps - 1 && (delta = this.demandArray[i] - this.demandArray[i + 1]) == 0) {
                i += 1;
                flatEnd = true;
            }
            /*
             * Add a point at i for the following two cases: 1) If not at the end of the demand array, add the next
             * point for i and remember that this point is the end of a flat segment. 2) Add a final point if past
             * the end of the demand array, but the previous point was not the beginning of a flat segment.
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
             * If not at the end of the demand array, check if the following segment is a step or an inclining
             * segment.
             */
            if (i < priceSteps - 1) {
                // TODO Test for floating point equality. Not just ==
                if (this.demandArray[i] - this.demandArray[i + 1] == delta) {
                    /*
                     * Here i is in a constantly inclining or declining segment. Search for the last price step in
                     * the segment.
                     */
                    while (i < priceSteps - 1 && this.demandArray[i] - this.demandArray[i + 1] == delta) {
                        i += 1;
                    }
                    /*
                     * If not at the end of the demand array, add the end point for the segment.
                     */
                    if (i < priceSteps - 1) {
                        points.add(newPoint(i));
                        i += 1;
                    }
                } else if (flatEnd && flatEndPoint != null) {
                    /*
                     * If i is not in a constantly inclining or declining segment, and the previous segment was
                     * flat, then move the end point of the flat segment one price step forward to convert it to a
                     * straight step.
                     * 
                     * This is to preserve the semantics of the straight step when converting between point and
                     * vector representation and back.
                     */
                	flatEndPoint = newPoint(i);
                }
            }

            /*
             * Add a point at i for the following two cases: 1) Add a point for the start of the next flat segment
             * and loop. 2) Add a final point if the last step of the demand array the end of an inclining or
             * declining segment.
             */
            if (i == priceSteps - 1 || (i < priceSteps - 1 && this.demandArray[i] - this.demandArray[i + 1] == 0)) {
                points.add(newPoint(i));
                flatStart = true;
            }
        }
        
		return points.toArray(new PricePoint[points.size()]);
	}
	
	private PricePoint newPoint(int priceStep) {
        return new PricePoint(new PriceStep(marketBasis, priceStep).toPrice(), this.demandArray[priceStep]);
    }
}
