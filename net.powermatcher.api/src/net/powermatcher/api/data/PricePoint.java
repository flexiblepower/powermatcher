package net.powermatcher.api.data;

public class PricePoint implements Comparable<PricePoint> {
    private final Price price;
    private final double demand;

    public PricePoint(Price price, double demand) {
        this.price = price;
        this.demand = demand;
    }

    public PricePoint(MarketBasis marketBasis, double price, double demand) {
        this(new Price(marketBasis, price), demand);
    }

    public Price getPrice() {
        return price;
    }

    public double getDemand() {
        return demand;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != getClass()) {
            return false;
        } else {
            PricePoint other = (PricePoint) obj;
            return other.price.equals(price) && other.demand == demand;
        }
    }

    @Override
    public int hashCode() {
        return 31 * price.hashCode() + Double.valueOf(demand).hashCode();
    }

    @Override
    public String toString() {
        return "PricePoint{" + price + ", demand = " + demand + ")";
    }

    @Override
    public int compareTo(PricePoint o) {
        int cmpPrice = this.price.compareTo(o.price);
        if (cmpPrice != 0) {
            return cmpPrice;
        } else if (this.demand < o.demand) {
            return -1;
        } else if (this.demand > o.demand) {
            return 1;
        } else {
            return 0;
        }
    }
}
