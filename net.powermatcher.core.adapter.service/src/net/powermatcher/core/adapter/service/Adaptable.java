package net.powermatcher.core.adapter.service;


/**
 * 
 * <p>
 * The AdapterService defines the generic interface of services provided by an
 * adapter.
 * </p>
 * <p>
 * An adapter provides one of possibly many implementations of services used by 
 * other components, including other adapters. Components can be extended or adapted
 * using a component that implements the AdapterService interface. The bind() method
 * will link the adapter to the component. The unbind() will break the connection.
 * </p>
 * 
 *  @author IBM
 * @version 0.9.0
 */
public interface Adaptable {
	/**
	 * Bind. Connect the adapter to the component.
	 * 
	 * @throws Exception
	 */
	public void bind() throws Exception;

	/**
	 * Gets the cluster ID (String) value.
	 * 
	 * @return The Cluster ID (<code>String</code>) value.
	 */
	public String getClusterId();

	/**
	 * Gets the ID (String) value.
	 * 
	 * @return Results of the get ID (<code>String</code>) value.
	 */
	public String getId();

	/**
	 * Gets the name (String) value.
	 * 
	 * @return Results of the get name (<code>String</code>) value.
	 */
	public String getName();

	/**
	 * Is enabled.
	 * 
	 * @return The is enabled (<code>boolean</code>) value.
	 */
	public boolean isEnabled();

	/**
	 * Unbind. Disconnect the adapter from the component.
	 */
	public void unbind();

}
