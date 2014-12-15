package net.powermatcher.api.data;

public class PriceUpdate {
    private final Price price;
	private final int bidNumber;

    public PriceUpdate(Price price, int bidNumber) {
    	if(price == null) {
    		throw new NullPointerException("price");
    	}
		this.price = price;
		this.bidNumber = bidNumber;
	}

    public Price getPrice() {
		return price;
	}

    public int getBidNumber() {
		return bidNumber;
	}

    @Override
    public boolean equals(Object obj) {
    	if(obj == this) {
    		return true;
    	} else if(obj == null || obj.getClass() != this.getClass()) {
    		return false;
    	} else {
    		PriceUpdate other = (PriceUpdate) obj;
    		return this.bidNumber == other.bidNumber && this.price.equals(other.price);
    	}
    }
    
    @Override
    public int hashCode() {
    	return 31 * price.hashCode() + bidNumber;
    }
    
    @Override
    public String toString() {
    	return "PriceUpdate [" + price + ", bidNr=" + bidNumber + "]";
    }
}
