package net.powermatcher.simulator.prototype.pmcore;

import java.util.Arrays;

public class Bid {
	public static final int LENGTH = 50;

	public double[] demand = new double[LENGTH];

	public static Bid mustRun(double powerFlow) {
		Bid bid = new Bid();
		Arrays.fill(bid.demand, powerFlow);
		return bid;
	}

	public double getAllocation(double price) {
		return demand[(int) Math.round(price)];
	}

	public Bid add(Bid bid) {
		Bid sum = new Bid();
		for (int i = 0; i < sum.demand.length; i++) {
			sum.demand[i] = this.demand[i] + bid.demand[i];
		}
		return sum;
	}

	public double getPrice(int target) {
		int low = 0;
		int high = demand.length - 1;

		while (high >= low) {
			int mid = (low + high) >>> 1;
			double midVal = demand[mid];

			if (midVal > target) {
				low = mid + 1;
			} else if (midVal < target) {
				high = mid - 1;
			} else {
				return mid;
			}
		}

		if (low == 0) {
			return low;
		} else if (high == demand.length - 1) {
			return high;
		} else {
			return high + (target - demand[low]) / (demand[high] - demand[low]);
			// return (double) (max - min) / 2.0 + (double) min;
		}
	}

	@Override
	public String toString() {
		return "[Bid " + this.demand[0] + " ... " + this.demand[this.demand.length - 1] + "]";
	}
}
