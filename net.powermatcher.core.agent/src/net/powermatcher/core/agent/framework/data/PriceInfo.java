package net.powermatcher.core.agent.framework.data;


/**
 * PriceInfo is an immutable type specifying a PowerMatcher market price.
 * 
 * @author IBM
 * @version 0.9.0
 */
public class PriceInfo {
	/**
	 * Equals with the specified obj1 and obj2 parameters and return the boolean
	 * result.
	 * 
	 * @param obj1
	 *            The obj1 (<code>Object</code>) parameter.
	 * @param obj2
	 *            The obj2 (<code>Object</code>) parameter.
	 * @return Results of the equals (<code>boolean</code>) value.
	 * @see #equals(Object)
	 */
	private static boolean equals(final Object obj1, final Object obj2) {
		return obj1 == obj2 || (obj1 != null && obj1.equals(obj2));
	}

	/**
	 * Define the market basis (MarketBasis) field.
	 */
	private MarketBasis marketBasis;

	/**
	 * Define the current price (double) field.
	 */
	private double currentPrice;

	/**
	 * Constructs an instance of this class from the specified market basis and
	 * current price parameters.
	 * 
	 * @param marketBasis
	 *            The market basis (<code>MarketBasis</code>) parameter.
	 * @param currentPrice
	 *            The current price (<code>double</code>) parameter.
	 */
	public PriceInfo(final MarketBasis marketBasis, final double currentPrice) {
		this.marketBasis = marketBasis;
		this.currentPrice = currentPrice;
	}

	/**
	 * Equals with the specified obj parameter and return the boolean result.
	 * 
	 * @param obj
	 *            The obj (<code>Object</code>) parameter.
	 * @return Results of the equals (<code>boolean</code>) value.
	 */
	@Override
	public boolean equals(final Object obj) {
		PriceInfo other = (PriceInfo) ((obj instanceof PriceInfo) ? obj : null);
		return this == other
				|| (other != null && other.currentPrice == this.currentPrice && equals(other.marketBasis, this.marketBasis));
	}

	/**
	 * Gets the current price (double) value.
	 * 
	 * @return The current price (<code>double</code>) value.
	 */
	public double getCurrentPrice() {
		return this.currentPrice;
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
	 * Gets the normalized price (int) value.
	 * 
	 * @return The normalized price (<code>int</code>) value.
	 */
	public int getNormalizedPrice() {
		return this.marketBasis.toNormalizedPrice(this.currentPrice);
	}

	/**
	 * Hash code and return the int result.
	 * 
	 * @return Results of the hash code (<code>int</code>) value.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		long temp = Double.doubleToLongBits(this.currentPrice);
		int result = prime + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((this.marketBasis == null) ? 0 : this.marketBasis.hashCode());
		return result;
	}

	/**
	 * To market basis with the specified new market basis parameter and return
	 * the BidInfo result.
	 * 
	 * @param newMarketBasis
	 *            The new market basis (<code>MarketBasis</code>) parameter.
	 * @return Results of the to market basis (<code>BidInfo</code>) value.
	 * @see #getMarketBasis()
	 */
	public PriceInfo toMarketBasis(final MarketBasis newMarketBasis) {
		if (this.marketBasis.equals(newMarketBasis)) {
			return this;
		} else {
			return new PriceInfo(newMarketBasis, this.currentPrice);
		}
	}

	/**
	 * Returns the string value.
	 * 
	 * @return The string (<code>String</code>) value.
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("PriceInfo{currentPrice=").append(MarketBasis.PRICE_FORMAT.format(this.currentPrice));
		b.append(", ").append(this.marketBasis);
		b.append('}');
		return b.toString();
	}

}
