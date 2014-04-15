package net.powermatcher.server.event.database.jdbc;


public class MarketBasisData {

	private int id;
	private String clusterId;
	private String commodity;
	private String currency;
	private float minPrice; 
	private float maxPrice; 
	private int priceSteps;
	
	

	public MarketBasisData() {
		super();
	}

	public MarketBasisData(int id, String clusterId, String commodity,
			String currency, float minPrice, float maxPrice, int priceSteps) {
		super();

		this.id = id;
		this.clusterId = clusterId;
		this.commodity = commodity;
		this.currency = currency;
		this.minPrice = minPrice;
		this.maxPrice = maxPrice;
		this.priceSteps = priceSteps;
	}
	
	public MarketBasisData(String clusterId, String commodity,
			String currency, float minPrice, float maxPrice, int priceSteps) {
		this(0, clusterId, commodity, currency, minPrice, maxPrice, priceSteps);
	}

	public int getId() {
		return this.id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getClusterId() {
		return this.clusterId;
	}
	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}
	public String getCommodity() {
		return this.commodity;
	}
	public void setCommodity(String commodity) {
		this.commodity = commodity;
	}
	public String getCurrency() {
		return this.currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}
	public float getMinPrice() {
		return this.minPrice;
	}
	public void setMinPrice(float minPrice) {
		this.minPrice = minPrice;
	}
	public float getMaxPrice() {
		return this.maxPrice;
	}
	public void setMaxPrice(float maxPrice) {
		this.maxPrice = maxPrice;
	}
	public int getPriceSteps() {
		return this.priceSteps;
	}
	public void setPriceSteps(int priceSteps) {
		this.priceSteps = priceSteps;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MarketBasisData other = (MarketBasisData) obj;
		if (clusterId == null) {
			if (other.clusterId != null)
				return false;
		} else if (!clusterId.equals(other.clusterId))
			return false;
		if (commodity == null) {
			if (other.commodity != null)
				return false;
		} else if (!commodity.equals(other.commodity))
			return false;
		if (currency == null) {
			if (other.currency != null)
				return false;
		} else if (!currency.equals(other.currency))
			return false;
		float epsilon = Math.abs(maxPrice / 10000);
		if (Math.abs(maxPrice - other.maxPrice) > epsilon)
			return false;
		if (Math.abs(minPrice - other.minPrice) > epsilon)
			return false;
		if (priceSteps != other.priceSteps)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clusterId == null) ? 0 : clusterId.hashCode());
		result = prime * result + ((commodity == null) ? 0 : commodity.hashCode());
		result = prime * result + ((currency == null) ? 0 : currency.hashCode());
		result = prime * result + Float.floatToIntBits(maxPrice);
		result = prime * result + Float.floatToIntBits(minPrice);
		result = prime * result + priceSteps;
		return result;
	}
}
