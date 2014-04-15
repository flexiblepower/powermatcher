package net.powermatcher.core.agent.framework.data;


/**
 * @author IBM
 * @version 0.9.0
 */
public class PricePoint {
	/**
	 * Define the normalized price (int) field.
	 */
	private int normalizedPrice;
	/**
	 * Define the demand (double) field.
	 */
	private double demand;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @see #PricePoint(int,double)
	 * @see #PricePoint(PricePoint)
	 * @see #PricePoint(String)
	 */
	public PricePoint() {
	}

	/**
	 * Constructs an instance of this class from the specified pricePoint parameter.
	 * 
	 * @param other
	 *            The other (<code>PricePoint</code>) parameter.
	 * @see #PricePoint()
	 * @see #PricePoint(int,double)
	 * @see #PricePoint(String)
	 */
	public PricePoint(PricePoint other) {
		this.normalizedPrice = other.normalizedPrice;
		this.demand = other.demand;
	}

	/**
	 * Constructs an instance of this class from the specified normalized price
	 * and demand parameters.
	 * 
	 * @param normalizedPrice
	 *            The nomalized price (<code>int</code>) parameter.
	 * @param demand
	 *            The demand (<code>double</code>) parameter.
	 * @see #PricePoint()
	 * @see #PricePoint(PricePoint)
	 * @see #PricePoint(String)
	 */
	public PricePoint(final int normalizedPrice, final double demand) {
		this.normalizedPrice = normalizedPrice;
		this.demand = demand;
	}

	/**
	 * Constructs an instance of this class from the specified str parameter.
	 * 
	 * @param str
	 *            The str (<code>String</code>) parameter.
	 * @see #PricePoint()
	 * @see #PricePoint(PricePoint)
	 * @see #PricePoint(int,double)
	 */
	public PricePoint(final String str) {
		String tokens = str;
		if (tokens.startsWith("(")) {
			tokens = tokens.substring(1);
		}
		if (tokens.endsWith(")")) {
			tokens = tokens.substring(0, tokens.length() - 1);
		}
		int p = tokens.indexOf(',');
		this.normalizedPrice = Integer.valueOf(tokens.substring(0, p));
		this.demand = Double.valueOf(tokens.substring(p + 1));
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
		PricePoint other = (PricePoint) ((obj instanceof PricePoint) ? obj : null);
		return this == other || (other != null && other.demand == this.demand && other.normalizedPrice == this.normalizedPrice);
	}

	/**
	 * Gets the demand (double) value.
	 * 
	 * @return The demand (<code>double</code>) value.
	 * @see #setDemand(double)
	 */
	public double getDemand() {
		return this.demand;
	}

	/**
	 * Gets the normalized price (int) value.
	 * 
	 * @return The normalized price (<code>int</code>) value.
	 * @see #setNormalizedPrice(int)
	 */
	public int getNormalizedPrice() {
		return this.normalizedPrice;
	}

	/**
	 * Hash code and return the int result.
	 * 
	 * @return Results of the hash code (<code>int</code>) value.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		long temp = Double.doubleToLongBits(this.demand);
		int result = prime + (int) (temp ^ (temp >>> 32));
		result = prime * result + this.normalizedPrice;
		return result;
	}

	/**
	 * Sets the demand value.
	 * 
	 * @param demand
	 *            The demand (<code>double</code>) parameter.
	 * @see #getDemand()
	 */
	public void setDemand(final double demand) {
		this.demand = demand;
	}

	/**
	 * Sets the normalized price value.
	 * 
	 * @param nomalizedPrice
	 *            The nomalized price (<code>int</code>) parameter.
	 * @see #getNormalizedPrice()
	 */
	public void setNormalizedPrice(final int nomalizedPrice) {
		this.normalizedPrice = nomalizedPrice;
	}

	/**
	 * Returns the string value.
	 * 
	 * @return The string (<code>String</code>) value.
	 */
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append('(').append(this.normalizedPrice);
		b.append(",").append(MarketBasis.DEMAND_FORMAT.format(this.demand));
		b.append(')');
		return b.toString();
	}

}
