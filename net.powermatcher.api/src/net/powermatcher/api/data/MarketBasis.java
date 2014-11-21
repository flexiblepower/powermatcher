package net.powermatcher.api.data;

import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * <p>
 * MarketBasis is an immutable type specifying the market basis for bids and prices.
 * </p>
 * 
 * <p>
 * It defines a market basis by commodity, currency, minimum price, maximum price, number of steps and significance. The
 * agent re-ceives market basis updates from its parent matcher
 * 
 * 
 * @author FAN
 * @version 1.0
 */
public class MarketBasis {
    /**
     * Define the root locale symbols (DecimalFormatSymbols) constant.
     */
    public static final DecimalFormatSymbols ROOT_SYMBOLS = DecimalFormatSymbols.getInstance(Locale.ROOT);
    /**
     * Define the price format (DecimalFormat) constant.
     */
    public static final DecimalFormat PRICE_FORMAT = new DecimalFormat("0.##", ROOT_SYMBOLS);
    /**
     * Define the demand format (DecimalFormat) constant.
     */
    public static final DecimalFormat DEMAND_FORMAT = new DecimalFormat("0.###E0", ROOT_SYMBOLS);

    /**
     * Define the commodity (String) field.
     */
    private String commodity;

    /**
     * Define the currency (String) field.
     */
    private String currency;

    /**
     * Define the price steps (int) field.
     */
    private int priceSteps;

    /**
     * Define the minimum price (double) field.
     */
    private double minimumPrice;

    /**
     * Define the maximum price (double) field.
     */
    private double maximumPrice;

    /**
     * Define the zero price step (int) field.
     */
    private int zeroPriceStep;

    /**
     * Constructs an instance of this class from the specified commodity, currency, price steps, minimum price, maximum
     * price, significance and market ref parameters.
     * 
     * @param commodity
     *            The commodity (<code>String</code>) parameter.
     * @param currency
     *            The currency (<code>String</code>) parameter.
     * @param priceSteps
     *            The price steps (<code>int</code>) parameter.
     * @param minimumPrice
     *            The minimum price (<code>double</code>) parameter.
     * @param maximumPrice
     *            The maximum price (<code>double</code>) parameter.
     * @param significance
     *            The significance (<code>int</code>) parameter.
     * @param marketRef
     *            The market ref (<code>int</code>) parameter.
     */
    public MarketBasis(final String commodity, final String currency, final int priceSteps, final double minimumPrice,
            final double maximumPrice) {
        if (priceSteps <= 0) {
            throw new InvalidParameterException("Price steps must be > 0.");
        }
        if (maximumPrice <= minimumPrice) {
            throw new InvalidParameterException("Maximum price must be > minimum price.");
        }
        this.commodity = commodity;
        this.currency = currency;
        this.priceSteps = priceSteps;
        this.minimumPrice = minimumPrice;
        this.maximumPrice = maximumPrice;
        this.zeroPriceStep = toPriceStep(0.0d);
    }

    /**
     * Bound normalized price with the specified normalized price parameter and return the int result.
     * 
     * @param normalizedPrice
     *            The normalized price (<code>int</code>) parameter.
     * @return Results of the bound normalized price (<code>int</code>) value.
     * @see #toNormalizedPrice(double)
     * @see #toNormalizedPrice(int)
     */
    public int boundNormalizedPrice(final int normalizedPrice) {
        int boundedNormalizedPrice = Math.min(normalizedPrice, this.priceSteps - this.zeroPriceStep - 1);
        boundedNormalizedPrice = Math.max(boundedNormalizedPrice, -this.zeroPriceStep);
        return boundedNormalizedPrice;
    }

    /**
     * Bound price with the specified price parameter and return the double result.
     * 
     * @param price
     *            The price (<code>double</code>) parameter.
     * @return Results of the bound price (<code>double</code>) value.
     * @see #boundNormalizedPrice(int)
     * @see #getMaximumPrice()
     * @see #getMinimumPrice()
     * @see #roundPrice(double)
     * @see #toNormalizedPrice(double)
     * @see #toNormalizedPrice(int)
     * @see #toPrice(int)
     */
    public double boundPrice(final double price) {
        double boundedPrice = Math.min(price, this.maximumPrice);
        boundedPrice = Math.max(boundedPrice, this.minimumPrice);
        return boundedPrice;
    }

    /**
     * Bound price step with the specified price step parameter and return the int result.
     * 
     * @param priceStep
     *            The price step (<code>int</code>) parameter.
     * @return Results of the bound price step (<code>int</code>) value.
     * @see #toPriceStep(double)
     * @see #toPriceStep(int)
     */
    public int boundPriceStep(final int priceStep) {
        int boundedPriceStep = Math.min(priceStep, this.priceSteps - 1);
        boundedPriceStep = Math.max(boundedPriceStep, 0);
        return boundedPriceStep;
    }

    /**
     * Round price with the specified price parameter and return the double result.
     * 
     * @param price
     *            The price (<code>double</code>) parameter.
     * @return Results of the round price (<code>double</code>) value.
     * @see #boundNormalizedPrice(int)
     * @see #boundPrice(double)
     * @see #getMaximumPrice()
     * @see #getMinimumPrice()
     * @see #toNormalizedPrice(double)
     * @see #toNormalizedPrice(int)
     * @see #toPrice(int)
     */
    public double roundPrice(final double price) {
        BigDecimal bd = new BigDecimal(Double.toString(price));
        return bd.doubleValue();
    }

    /**
     * To normalized price with the specified price parameter and return the int result.
     * 
     * @param price
     *            The price (<code>double</code>) parameter.
     * @return Results of the to normalized price (<code>int</code>) value.
     * @see #boundNormalizedPrice(int)
     * @see #toNormalizedPrice(int)
     */
    public int toNormalizedPrice(final double price) {
        return toPriceStep(price) - this.zeroPriceStep;
    }

    /**
     * To normalized price with the specified price step parameter and return the int result.
     * 
     * @param priceStep
     *            The price step (<code>int</code>) parameter.
     * @return Results of the to normalized price (<code>int</code>) value.
     * @see #boundNormalizedPrice(int)
     * @see #toNormalizedPrice(double)
     */
    public int toNormalizedPrice(final int priceStep) {
        return priceStep - this.zeroPriceStep;
    }

    /**
     * To price with the specified price step parameter and return the double result.
     * 
     * @param priceStep
     *            The price step (<code>int</code>) parameter.
     * @return Results of the to price (<code>double</code>) value.
     * @see #boundNormalizedPrice(int)
     * @see #boundPrice(double)
     * @see #getMaximumPrice()
     * @see #getMinimumPrice()
     * @see #roundPrice(double)
     * @see #toNormalizedPrice(double)
     * @see #toNormalizedPrice(int)
     */
    public double toPrice(final int priceStep) {
        return roundPrice(this.minimumPrice + priceStep
                * ((this.maximumPrice - this.minimumPrice) / (this.priceSteps - 1)));
    }

    /**
     * To price step with the specified price parameter and return the int result.
     * 
     * @param price
     *            The price (<code>double</code>) parameter.
     * @return Results of the to price step (<code>int</code>) value.
     * @see #boundPriceStep(int)
     * @see #toPriceStep(int)
     */
    public int toPriceStep(final double price) {
        double priceStep = ((price - this.minimumPrice) / (this.maximumPrice - this.minimumPrice))
                * (this.priceSteps - 1);
        return Math.round((float) priceStep);
    }

    /**
     * To price step with the specified normalized price parameter and return the int result.
     * 
     * @param normalizedPrice
     *            The normalized price (<code>int</code>) parameter.
     * @return Results of the to price step (<code>int</code>) value.
     * @see #boundPriceStep(int)
     * @see #toPriceStep(double)
     */
    public int toPriceStep(final int normalizedPrice) {
        return this.zeroPriceStep + normalizedPrice;
    }

    /**
     * Gets the commodity (String) value.
     * 
     * @return The commodity (<code>String</code>) value.
     */
    public String getCommodity() {
        return this.commodity;
    }

    /**
     * Gets the 3 character currency code value.
     * 
     * @return The currency (<code>String</code>) value.
     */
    public String getCurrency() {
        return this.currency;
    }

    /**
     * Gets the maximum price (double) value.
     * 
     * @return The maximum price (<code>double</code>) value.
     */
    public double getMaximumPrice() {
        return this.maximumPrice;
    }

    /**
     * Gets the minimum price (double) value.
     * 
     * @return The minimum price (<code>double</code>) value.
     */
    public double getMinimumPrice() {
        return this.minimumPrice;
    }

    /**
     * Gets the price increment (double) value.
     * 
     * @return The price increment (<code>double</code>) value.
     */
    public final double getPriceIncrement() {
        return (this.maximumPrice - this.minimumPrice) / (this.priceSteps - 1);
    }

    /**
     * Gets the price steps (int) value.
     * 
     * @return The price steps (<code>int</code>) value.
     */
    public int getPriceSteps() {
        return this.priceSteps;
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
        MarketBasis other = (MarketBasis) ((obj instanceof MarketBasis) ? obj : null);
        // TODO Reduce the number of conditional operators (6) used in the
        // expression (maximum allowed 3).
        return this == other
                || (other != null && this.commodity.equals(other.commodity) && this.currency.equals(other.currency)
                        && other.priceSteps == this.priceSteps && other.minimumPrice == this.minimumPrice && other.maximumPrice == this.maximumPrice);
    }

    /**
     * Hash code and return the int result.
     * 
     * @return Results of the hash code (<code>int</code>) value.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime + ((this.commodity == null) ? 0 : this.commodity.hashCode());
        result = prime * result + ((this.currency == null) ? 0 : this.currency.hashCode());
        long temp = Double.doubleToLongBits(this.maximumPrice);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.minimumPrice);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + this.priceSteps;
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
        b.append("MarketBasis{commodity=").append(this.commodity);
        b.append(", currency=").append(this.currency);
        b.append(", minimumPrice=").append(PRICE_FORMAT.format(this.minimumPrice));
        b.append(", maximumPrice=").append(PRICE_FORMAT.format(this.maximumPrice));
        b.append(", priceSteps=").append(this.priceSteps);
        b.append('}');
        return b.toString();
    }

}
