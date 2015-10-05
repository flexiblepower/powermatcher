// package net.powermatcher.api.data;
//
/// **
// * This immutable abstract data object defines the basis for a OldBid in the Powermatcher cluster. The OldBid curve
// can
// * be represented in several ways, so subclasses will have their own implementation.
// *
// * @author FAN
// * @version 2.0
// */
// public abstract class OldOldBid {
// /**
// * The smallest difference between demands, for them to be called different.
// */
// protected static final double SMALLEST_DEMAND = 1e-6;
//
// /**
// * Tests if the difference between the 2 demand values is less that {@link #SMALLEST_DEMAND}.
// *
// * @param demand1
// * The first demand
// * @param demand2
// * The second demand
// * @return true when the difference between the 2 demand values is less than {@link #SMALLEST_DEMAND}
// */
// protected static boolean demandIsEqual(double demand1, double demand2) {
// return Math.abs(demand1 - demand2) < SMALLEST_DEMAND;
// }
//
// /**
// * Creates a new flat {@link OldBid} with a given demand.
// *
// * @param marketBasis
// * The {@link MarketBasis} on which the {@link OldBid} should be based
// * @param demand
// * The constant demand value
// * @return A new {@link OldBid} that represents a flat OldBid
// */
// public static OldBid flatDemand(MarketBasis marketBasis, double demand) {
// return new PointOldBid.Builder(marketBasis).add(marketBasis.getMinimumPrice(), demand).build();
// }
//
// /**
// * The {@link MarketBasis} of the cluster.
// */
// protected final MarketBasis marketBasis;
//
// /**
// * A constructor used to create an instance of this class.
// *
// * @param marketBasis
// * the {@link MarketBasis} of the cluster.
// */
// protected OldOldBid(MarketBasis marketBasis) {
// if (marketBasis == null) {
// throw new IllegalArgumentException("marketBasis is not allowed to be null");
// }
// this.marketBasis = marketBasis;
// }
//
// /**
// * @return the current value of marketBasis.
// */
// public MarketBasis getMarketBasis() {
// return marketBasis;
// }
//
// /**
// * Aggregates this {@link OldOldBid} instance with another OldOldBid. OldOldBid are aggregated by adding their
// * OldOldBid curves.
// *
// * @param other
// * The {@link OldOldBid} whose OldOldBid curve has to be added to the OldBid curve of this instance.
// * @return A new aggregated OldBid.
// */
// public abstract OldBid aggregate(final OldBid other);
//
// /**
// * Calculates the {@link Price} at the intersection with the OldBid curve at the given demand.
// *
// * @param targetDemand
// * the part of the OldBid curve you want to get the {@link Price} of.
// * @return the calculated{@link Price}
// */
// public abstract Price calculateIntersection(double targetDemand);
//
// /**
// * @return the highest demand in the demand curve.
// */
// public abstract double getMaximumDemand();
//
// /**
// * @return the lowest demand in the demand curve.
// */
// public abstract double getMinimumDemand();
//
// /**
// * @return The {@link ArrayOldBid} implementation of this OldBid.
// */
// public abstract ArrayBid toArrayOldBid();
//
// /**
// * @return The {@link PointOldBid} implementation of this OldBid.
// */
// public abstract PointBid toPointOldBid();
//
// /**
// * Calculates the demand at the intersection with the OldBid curve at the given {@link PriceStep}.
// *
// * Implementation note: you should always override either this method or the {@link #getDemandAt(Price)} method. The
// * default implementation is to call the other.
// *
// * @param priceStep
// * the {@link PriceStep} you want to know the demand of.
// * @return the calculated demand
// */
// public double getDemandAt(PriceStep priceStep) {
// return getDemandAt(priceStep.toPrice());
// }
//
// /**
// * Calculates the demand at the intersection in the OldBid curve with the priceStep in a demand array.
// *
// * Implementation note: you should always override either this method or the {@link #getDemandAt(PriceStep)} method.
// * The default implementation is to call the other.
// *
// * @param price
// * the {@link Price} you want to know the demand of.
// * @return the calculated demand
// */
// public double getDemandAt(Price price) {
// return getDemandAt(price.toPriceStep());
// }
// }
