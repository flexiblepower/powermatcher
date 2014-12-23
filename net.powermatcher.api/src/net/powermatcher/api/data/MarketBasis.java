package net.powermatcher.api.data;

import java.security.InvalidParameterException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * {@link MarketBasis} is an immutable type specifying the settings for the market. This includes the price range, the
 * commodity being exchanges, the currency being used and the number of price steps used in the demand arrays.
 * 
 * @author FAN
 * @version 2.0
 */
public class MarketBasis {
    /**
     * A {@link DecimalFormatSymbols} that is language/country-neutral
     */
    public static final DecimalFormatSymbols ROOT_SYMBOLS = DecimalFormatSymbols.getInstance(Locale.ROOT);
    /**
     * The {@link DecimalFormat} that should be used to print prices
     */
    public static final DecimalFormat PRICE_FORMAT = new DecimalFormat("0.##", ROOT_SYMBOLS);
    /**
     * The {@link DecimalFormat} that should be used to print demand
     */
    public static final DecimalFormat DEMAND_FORMAT = new DecimalFormat("0.###E0", ROOT_SYMBOLS);

    private final String commodity;
    private final String currency;
    private final int priceSteps;
    private final double minimumPrice;
    private final double maximumPrice;

    public MarketBasis(final String commodity, final String currency, final int priceSteps, final double minimumPrice,
            final double maximumPrice) {
        if (commodity == null) {
            throw new NullPointerException("commodity");
        } else if (currency == null) {
            throw new NullPointerException("currency");
        } else if (priceSteps <= 0) {
            throw new InvalidParameterException("Price steps must be > 0.");
        } else if (Double.isNaN(minimumPrice)) {
            throw new IllegalArgumentException("minimumPrice should not be NaN");
        } else if (Double.isNaN(maximumPrice)) {
            throw new IllegalArgumentException("maximumPrice should not be NaN");
        } else if (maximumPrice <= minimumPrice) {
            throw new InvalidParameterException("Maximum price must be > minimum price.");
        }

        this.commodity = commodity;
        this.currency = currency;
        this.priceSteps = priceSteps;
        this.minimumPrice = minimumPrice;
        this.maximumPrice = maximumPrice;
    }

    /**
     * @return The commodity that is to be handled by this market. E.g. electricity
     */
    public String getCommodity() {
        return this.commodity;
    }

    /**
     * @return The 3 character currency code. E.g. EUR
     */
    public String getCurrency() {
        return this.currency;
    }

    /**
     * @return The maximum price that is valid in this market (inclusive)
     */
    public double getMaximumPrice() {
        return this.maximumPrice;
    }

    /**
     * @return The minimum price that is valid in this market (inclusive)
     */
    public double getMinimumPrice() {
        return this.minimumPrice;
    }

    /**
     * @return The difference in market price between a price step
     */
    public final double getPriceIncrement() {
        return (this.maximumPrice - this.minimumPrice) / (this.priceSteps - 1);
    }

    /**
     * @return The number of price steps used when converting a {@link PointBid} to an {@link ArrayBid}
     */
    public int getPriceSteps() {
        return this.priceSteps;
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
    public int boundPriceStep(final PriceStep priceStep) {
        int step = priceStep.getPriceStep();
        int boundedPriceStep = Math.min(step, this.priceSteps - 1);
        boundedPriceStep = Math.max(boundedPriceStep, 0);
        return boundedPriceStep;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != getClass()) {
            return false;
        } else {
            MarketBasis other = (MarketBasis) obj;
            return this.commodity.equals(other.commodity) && this.currency.equals(other.currency)
                    && this.maximumPrice == other.maximumPrice && this.minimumPrice == other.minimumPrice
                    && this.priceSteps == other.priceSteps;
        }
    }

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
