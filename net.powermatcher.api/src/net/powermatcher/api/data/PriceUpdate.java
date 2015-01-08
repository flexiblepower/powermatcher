package net.powermatcher.api.data;

/**
 * 
 * This immutable data object links a {@link PriceUpdate} with a {@link Bid},
 * through its bidNumber. PriceUpdate are send between Agents and make sure a
 * Price stays linked with a {@link Bid}
 * 
 * @author FAN
 * @version 2.0
 */
public class PriceUpdate {

	/**
	 * The {@link Price} of this instance.
	 */
	private final Price price;
	/**
	 * The bidNumber of the {@link Bid} that was the basis of this {@link Price}
	 * .
	 */
	private final int bidNumber;

	/**
	 * A constructor used to create a new PriceUpdate instance.
	 * 
	 * @param price
	 *            the {@link Price} of this PriceUpdate
	 * @param bidNumber
	 *            the bidNumber of the {@link Bid} that was the basis of this
	 *            {@link Price}.
	 */
	public PriceUpdate(Price price, int bidNumber) {
		if (price == null) {
			throw new NullPointerException("price");
		}
		this.price = price;
		this.bidNumber = bidNumber;
	}

	/**
	 * @return the current value of price.
	 */
	public Price getPrice() {
		return price;
	}

	/**
	 * @return the current value of bidNumber.
	 */
	public int getBidNumber() {
		return bidNumber;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		} else {
			PriceUpdate other = (PriceUpdate) obj;
			return this.bidNumber == other.bidNumber
					&& this.price.equals(other.price);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return 31 * price.hashCode() + bidNumber;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "PriceUpdate [" + price + ", bidNr=" + bidNumber + "]";
	}
}
