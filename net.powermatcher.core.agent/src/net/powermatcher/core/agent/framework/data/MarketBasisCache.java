package net.powermatcher.core.agent.framework.data;


import java.util.HashMap;
import java.util.Map;

import net.powermatcher.core.agent.framework.service.MarketBasisMappingInterface;


/**
 * @author IBM
 * @version 0.9.0
 */
public class MarketBasisCache {
	/**
	 * Define the external market basis cache (Map<Integer,MarketBasis>) field.
	 */
	private Map<Integer, MarketBasis> externalMarketBasisCache = new HashMap<Integer, MarketBasis>();
	/**
	 * Define the internal market basis cache (Map<Integer,MarketBasis>) field.
	 */
	private Map<Integer, MarketBasis> internalMarketBasisCache = new HashMap<Integer, MarketBasis>();
	/**
	 * Define the mapper (MarketBasisMappingService) field.
	 */
	private MarketBasisMappingInterface mapper;

	/**
	 * Constructs an instance of this class from the specified mapper parameter.
	 * 
	 * @param mapper
	 *            The mapper (<code>MarketBasisMappingInterface</code>)
	 *            parameter.
	 */
	public MarketBasisCache(final MarketBasisMappingInterface mapper) {
		this.mapper = mapper;
	}

	/**
	 * Get external market basis with the specified market ref parameter and
	 * return the MarketBasis result.
	 * 
	 * @param marketRef
	 *            The market ref (<code>int</code>) parameter.
	 * @return Results of the get external market basis (
	 *         <code>MarketBasis</code>) value.
	 * @see #getExternalMarketBasis(MarketBasis)
	 * @see #registerExternalMarketBasis(MarketBasis)
	 */
	public synchronized MarketBasis getExternalMarketBasis(final int marketRef) {
		Integer key = getKey(marketRef);
		return this.externalMarketBasisCache.get(key);
	}

	/**
	 * Get external market basis with the specified market basis parameter and
	 * return the MarketBasis result.
	 * 
	 * @param internalMarketBasis
	 *            The internal market basis (<code>MarketBasis</code>)
	 *            parameter.
	 * @return Results of the get external market basis (
	 *         <code>MarketBasis</code>) value.
	 * @see #getExternalMarketBasis(int)
	 * @see #registerExternalMarketBasis(MarketBasis)
	 */
	public synchronized MarketBasis getExternalMarketBasis(final MarketBasis internalMarketBasis) {
		Integer key = getKey(internalMarketBasis.getMarketRef());
		if (internalMarketBasis.equals(this.internalMarketBasisCache.get(key))) {
			return this.externalMarketBasisCache.get(key);
		}
		MarketBasis externalMarketBasis = this.mapper.toExternalMarketBasis(internalMarketBasis);
		this.internalMarketBasisCache.put(key, internalMarketBasis);
		this.externalMarketBasisCache.put(key, externalMarketBasis);
		return externalMarketBasis;
	}

	/**
	 * Get internal market basis with the specified market ref parameter and
	 * return the MarketBasis result.
	 * 
	 * @param marketRef
	 *            The market ref (<code>int</code>) parameter.
	 * @return Results of the get internal market basis (
	 *         <code>MarketBasis</code>) value.
	 * @see #getInternalMarketBasis(MarketBasis)
	 * @see #registerInternalMarketBasis(MarketBasis)
	 */
	public synchronized MarketBasis getInternalMarketBasis(final int marketRef) {
		Integer key = getKey(marketRef);
		return this.internalMarketBasisCache.get(key);
	}

	/**
	 * Get internal market basis with the specified market basis parameter and
	 * return the MarketBasis result.
	 * 
	 * @param externalMarketBasis
	 *            The external market basis (<code>MarketBasis</code>)
	 *            parameter.
	 * @return Results of the get internal market basis (
	 *         <code>MarketBasis</code>) value.
	 * @see #getInternalMarketBasis(int)
	 * @see #registerInternalMarketBasis(MarketBasis)
	 */
	public synchronized MarketBasis getInternalMarketBasis(final MarketBasis externalMarketBasis) {
		Integer key = getKey(externalMarketBasis.getMarketRef());
		if (externalMarketBasis.equals(this.externalMarketBasisCache.get(key))) {
			return this.internalMarketBasisCache.get(key);
		}
		MarketBasis internalMarketBasis = this.mapper.toInternalMarketBasis(externalMarketBasis);
		this.internalMarketBasisCache.put(key, internalMarketBasis);
		this.externalMarketBasisCache.put(key, externalMarketBasis);
		return internalMarketBasis;
	}

	/**
	 * Get key with the specified market ref parameter and return the Integer
	 * result.
	 * 
	 * @param marketRef
	 *            The market ref (<code>int</code>) parameter.
	 * @return Results of the get key (<code>Integer</code>) value.
	 */
	private Integer getKey(final int marketRef) {
		return Integer.valueOf(marketRef);
	}

	/**
	 * Get key with the specified market basis parameter and return the Integer
	 * result.
	 * 
	 * @param marketBasis
	 *            The market basis (<code>MarketBasis</code>) parameter.
	 * @return Results of the get key (<code>Integer</code>) value.
	 */
	private Integer getKey(final MarketBasis marketBasis) {
		return getKey(marketBasis.getMarketRef());
	}

	/**
	 * Register an external market basis in the cache, or return the currently
	 * registered instance if the same market basis is already in the case.
	 * 
	 * @param marketBasis
	 *            The market basis (<code>MarketBasis</code>) parameter.
	 * @return Results of the register external market basis (
	 *         <code>MarketBasis</code>) value.
	 * @see #getExternalMarketBasis(int)
	 * @see #getExternalMarketBasis(MarketBasis)
	 */
	public synchronized MarketBasis registerExternalMarketBasis(final MarketBasis marketBasis) {
		Integer key = getKey(marketBasis.getMarketRef());
		MarketBasis externalMarketBasis = this.externalMarketBasisCache.get(key);
		if (!marketBasis.equals(externalMarketBasis)) {
			externalMarketBasis = marketBasis;
			MarketBasis internalMarketBasis = this.mapper.toInternalMarketBasis(externalMarketBasis);
			this.internalMarketBasisCache.put(key, internalMarketBasis);
			this.externalMarketBasisCache.put(key, externalMarketBasis);
		}
		return externalMarketBasis;
	}

	/**
	 * Register an internal market basis in the cache, or return the currently
	 * registered instance if the same market basis is already in the case.
	 * 
	 * @param marketBasis
	 *            The market basis (<code>MarketBasis</code>) parameter.
	 * @return Results of the register internal market basis (
	 *         <code>MarketBasis</code>) value.
	 * @see #getInternalMarketBasis(MarketBasis)
	 * @see #getInternalMarketBasis(int)
	 */
	public synchronized MarketBasis registerInternalMarketBasis(final MarketBasis marketBasis) {
		Integer key = getKey(marketBasis.getMarketRef());
		MarketBasis internalMarketBasis = this.internalMarketBasisCache.get(key);
		if (!marketBasis.equals(internalMarketBasis)) {
			internalMarketBasis = marketBasis;
			MarketBasis externalMarketBasis = this.mapper.toExternalMarketBasis(internalMarketBasis);
			this.internalMarketBasisCache.put(key, internalMarketBasis);
			this.externalMarketBasisCache.put(key, externalMarketBasis);
		}
		return internalMarketBasis;
	}

	/**
	 * Remove internal and external marketbasis with the specified market ref
	 * parameter.
	 * 
	 * @param marketRef
	 *            The market ref (<code>int</code>) parameter.
	 * @see #remove(MarketBasis)
	 */
	public synchronized void remove(final int marketRef) {
		Integer key = getKey(marketRef);
		this.internalMarketBasisCache.remove(key);
		this.externalMarketBasisCache.remove(key);
	}

	/**
	 * Remove internal and external marketbasis with the specified market basis
	 * parameter.
	 * 
	 * @param marketBasis
	 *            The market basis (<code>MarketBasis</code>) parameter.
	 * @see #remove(int)
	 */
	public synchronized void remove(final MarketBasis marketBasis) {
		Integer key = getKey(marketBasis);
		MarketBasis internalMarketBasis = this.internalMarketBasisCache.get(key);
		MarketBasis externalMarketBasis = this.externalMarketBasisCache.get(key);
		if (marketBasis.equals(internalMarketBasis) || marketBasis.equals(externalMarketBasis)) {
			this.internalMarketBasisCache.remove(key);
			this.externalMarketBasisCache.remove(key);
		}
	}

}
