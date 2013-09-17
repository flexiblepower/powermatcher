package net.powermatcher.fpai.agent;

import net.powermatcher.core.agent.framework.data.BidInfo;

import org.flexiblepower.rai.unit.PowerUnit;
import org.flexiblepower.rai.values.PowerConstraint;
import org.flexiblepower.rai.values.PowerConstraintList;
import org.flexiblepower.rai.values.PowerValue;

public class BidUtils {

    public static BidInfo roundBidToPowerConstraintList(BidInfo bid, PowerConstraintList pcl, boolean includeZero) {
        if (includeZero) {
            // Make a clone of the PowerConstraintList that includes a demand of 0 Watt
            PowerConstraint[] values = pcl.toArray(new PowerConstraint[pcl.size() + 1]);
            values[values.length - 1] = new PowerConstraint(new PowerValue(0, PowerUnit.WATT));
            pcl = new PowerConstraintList(values);
        }
        double[] demand = bid.getDemand();
        for (int i = 0; i < demand.length; i++) {
            demand[i] = roundToPowerConstraintList(pcl, demand[i]).getValueAs(PowerUnit.WATT);
        }
        return new BidInfo(bid.getMarketBasis(), demand);
    }

    public static PowerValue roundToPowerConstraintList(PowerConstraintList pcl, double wantedPower) {
        PowerValue resultValue = null;
        double result = Double.NaN;

        for (PowerConstraint pc : pcl) {
            PowerValue powerValue = pc.getClosestValue(new PowerValue(wantedPower, PowerUnit.WATT));
            double power = powerValue.getValueAs(PowerUnit.WATT);

            if (resultValue == null || (Math.abs(power - wantedPower) < Math.abs(result - wantedPower))) {
                resultValue = powerValue;
                result = power;
            }
        }

        return resultValue;
    }

    /**
     * Transform a bid so that it has at least a demand of minDemandWatt for every price.
     * 
     * @param bid
     *            The bid to transform
     * @param pcl
     *            The allowed power values
     * @param minDemandWatt
     *            The minimum demand which is allowed (will be adjusted to the given power constraint list)
     * @return The transformed bid wherein any demand in the original bid is adjusted so that it is not below the given
     *         minimum demand (as rounded to the nearest allowed power value in the given constraint list).
     */
    public static BidInfo setMinimumDemand(BidInfo bid, PowerConstraintList pcl, double minDemandWatt) {
        minDemandWatt = roundToPowerConstraintList(pcl, minDemandWatt).getValueAs(PowerUnit.WATT);

        double[] demand = bid.getDemand();
        for (int i = 0; i < demand.length; i++) {
            demand[i] = Math.max(demand[i], minDemandWatt);
        }

        return new BidInfo(bid.getMarketBasis(), demand);
    }

    /**
     * Transform a bid so that it has at most a demand of maxDemandWatt for every price.
     * 
     * @param bid
     *            The bid to transform
     * @param pcl
     *            The allowed power values
     * @param maxDemandWatt
     *            The maximum demand which is allowed (will be adjusted to the given power constraint list)
     * @return The transformed bid wherein any demand in the original bid is adjusted so that it is not above the given
     *         maximum demand (as rounded to the nearest allowed power value in the given constraint list).
     */
    public static BidInfo setMaximumDemand(BidInfo bid, PowerConstraintList pcl, double maxDemandWatt) {
        maxDemandWatt = roundToPowerConstraintList(pcl, maxDemandWatt).getValueAs(PowerUnit.WATT);

        double[] demand = bid.getDemand();
        for (int i = 0; i < demand.length; i++) {
            demand[i] = Math.min(demand[i], maxDemandWatt);
        }

        return new BidInfo(bid.getMarketBasis(), demand);
    }

    // returns null when not found
    public static PowerValue floorToPowerConstraintList(PowerConstraintList pcl, double wantedPowerWatt) {
        Double result = null;

        for (PowerConstraint pc : pcl) {
            double upper = pc.getUpperBound().getValueAs(PowerUnit.WATT);
            double lower = pc.getLowerBound().getValueAs(PowerUnit.WATT);
            if (upper >= wantedPowerWatt && lower <= wantedPowerWatt) {
                return new PowerValue(wantedPowerWatt, PowerUnit.WATT);
            } else if (upper < wantedPowerWatt) {
                if (result == null || upper > result) {
                    result = lower;
                }
            }
        }

        if (result == null) {
            return null;
        } else {
            return new PowerValue(result, PowerUnit.WATT);
        }
    }

    // TODO move to PowerConstraintList?
    public static PowerValue ceilToPowerConstraintList(PowerConstraintList pcl, double wantedPowerWatt) {
        Double result = null;

        for (PowerConstraint pc : pcl) {
            double upper = pc.getUpperBound().getValueAs(PowerUnit.WATT);
            double lower = pc.getLowerBound().getValueAs(PowerUnit.WATT);
            if (upper >= wantedPowerWatt && lower <= wantedPowerWatt) {
                return new PowerValue(wantedPowerWatt, PowerUnit.WATT);
            } else if (lower > wantedPowerWatt) {
                if (result == null || lower < result) {
                    result = lower;
                }
            }
        }

        if (result == null) {
            return null;
        } else {
            return new PowerValue(result, PowerUnit.WATT);
        }
    }

    // TODO move to PowerConstraintList?
    public static PowerValue highestPowerValue(PowerConstraintList pcl) {
        PowerConstraint highest = null;
        for (PowerConstraint pc : pcl) {
            if (highest == null || pc.getUpperBound().getValueAs(PowerUnit.WATT) > highest.getUpperBound()
                                                                                          .getValueAs(PowerUnit.WATT)) {
                highest = pc;
            }
        }
        return highest.getUpperBound();
    }

    // TODO move to PowerConstraintList?
    public static PowerValue lowestPowerValue(PowerConstraintList pcl) {
        PowerConstraint lowest = null;
        for (PowerConstraint pc : pcl) {
            if (lowest == null || pc.getLowerBound().getValueAs(PowerUnit.WATT) < lowest.getLowerBound()
                                                                                        .getValueAs(PowerUnit.WATT)) {
                lowest = pc;
            }
        }
        return lowest.getLowerBound();
    }
}
