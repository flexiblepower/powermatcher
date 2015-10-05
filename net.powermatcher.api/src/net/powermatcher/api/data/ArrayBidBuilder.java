package net.powermatcher.api.data;

import java.util.Arrays;
import java.util.zip.Checksum;

/**
 * A builder class to create an {@link Bid} instance.
 *
 * @author FAN
 * @version 2.0
 */
public final class ArrayBidBuilder {

    /**
     * The {@link MarketBasis} of the cluster.
     */
    private final MarketBasis marketBasis;

    /**
     * The index of the next item the demand array.
     */
    private int nextIndex;

    /**
     * The demandArray that will be filled in this builder.
     */
    private double[] builderDemand;

    /**
     * Constructor to create an instance of this class.
     *
     * @param marketBasis
     *            the {@link MarketBasis} of the cluster.
     */
    public ArrayBidBuilder(MarketBasis marketBasis) {
        this.marketBasis = marketBasis;
        nextIndex = 0;
        builderDemand = new double[marketBasis.getPriceSteps()];
    }

    /**
     * Adds a demand <code>double</code> to builderDemand.
     *
     * @param demand
     *            The demand to be added to the demand array. Must not be higher than the previous added demand.
     * @return this instance of the Builder, with the added demand
     * @throws IllegalArgumentException
     *             when the demand to be added is higher than the demand already in the array
     * @throws ArrayIndexOutOfBoundsException
     *             when the demand array is already full
     */
    public ArrayBidBuilder demand(double demand) {
        checkIndex(nextIndex);
        if (nextIndex > 0 && demand > builderDemand[nextIndex - 1]) {
            throw new IllegalArgumentException("The demand can not be ascending");
        }
        builderDemand[nextIndex++] = demand;
        return this;
    }

    /**
     * {@link Checksum} to see if the given index is not bigger than the length of builderDemand.
     *
     * @param ix
     *            the index that has to be checked
     * @throws ArrayIndexOutOfBoundsException
     *             if the given index is bigger than the length of builderDemand.
     */
    private void checkIndex(int ix) {
        if (ix >= builderDemand.length) {
            throw new ArrayIndexOutOfBoundsException("Demand array has already been filled to maximum");
        }
    }

    /**
     * Sets the demandArray with the supplied demand array. The supplied array must not be ascending. The length of the
     * array should be the same size as the number of price steps in the marketBasis.
     *
     * @param demand
     *            The new demand array
     * @return this instance of the Builder, with the added array
     * @throws IllegalArgumentException
     *             if the size of the array differs from the number of priceSteps in the MarketBasis or if the demand
     *             array is ascending
     */
    public ArrayBidBuilder demandArray(double... demand) {
        if (demand.length != marketBasis.getPriceSteps()) {
            throw new IllegalArgumentException("supplied array is not same size as number of priceSteps in MarketBasis");
        }
        Bid.checkDescending(demand);
        builderDemand = Arrays.copyOf(demand, demand.length);
        nextIndex = builderDemand.length;
        return this;
    }

    /**
     * Makes sure the whole array is filled, then creates the Bid with the Builder's internal values.
     *
     * @return The created Bid
     * @throws IllegalArgumentException
     *             When the length of the demandArray is not equal to the number of price steps
     */
    public Bid build() {
        fillTo(builderDemand.length);
        return new Bid(marketBasis, builderDemand);
    }

    /**
     * fills the demand array from the nextIndex until the designated priceStep with the last set demand Will do nothing
     * on an already filled array.
     *
     * @param priceStep
     *            The priceStep to fill to.
     * @return this instance of the Builder, with the filled demand array
     * @throws IllegalStateException
     *             when nextIndex is zero (no demand has been set, yet)
     * @throws IllegalArgumentException
     *             when the supplied priceStep is higher than the number of price steps in the marketBasis
     */
    public ArrayBidBuilder fillTo(int priceStep) {
        if (nextIndex == 0) {
            throw new IllegalStateException("Demand array contains no demand that can be extended");
        }
        if (priceStep > marketBasis.getPriceSteps()) {
            throw new IllegalArgumentException("The supplied priceStep is out of bounds");
        }
        double demand = builderDemand[nextIndex - 1];
        while (nextIndex < priceStep) {
            builderDemand[nextIndex++] = demand;
        }
        return this;
    }
}
