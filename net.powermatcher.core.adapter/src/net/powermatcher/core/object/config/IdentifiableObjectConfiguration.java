package net.powermatcher.core.object.config;

import net.powermatcher.core.object.IdentifiableObject;


/**
 * 
 * <p>
 * Defines the property names of the configuration object
 * that sets the properties of the IdentifiableObject instance.
 * </p>
 * 
 * @author IBM
 * @version 0.9.0
 * 
 * @see IdentifiableObject
 */
public interface IdentifiableObjectConfiguration {
	/**
	 * Define the cluster ID property (String) name.
	 */
	public static final String CLUSTER_ID_PROPERTY = "cluster.id";
	/**
	 * Define the cluster ID default (String) constant.
	 */
	public static final String CLUSTER_ID_DEFAULT = "DefaultCluster";
	/**
	 * Define the cluster ID description (String) constant.
	 */
	public static final String CLUSTER_ID_DESCRIPTION = "Cluster ID";
	/**
	 * Define the id property (String) name.
	 */
	public static final String ID_PROPERTY = "id";
	/**
	 * Define the id description (String) name.
	 */
	public static final String ID_DESCRIPTION = "Component ID";

	/**
	 * Define the enabled property (String) constant.
	 */
	public static final String ENABLED_PROPERTY = "enabled";
	/**
	 * Define the enabled default (boolean) constant.
	 */
	public static final boolean ENABLED_DEFAULT = true;
	/**
	 * Define the enabled default str (String) constant.
	 */
	public static final String ENABLED_DEFAULT_STR = "true";
	/**
	 * Define the enabled description (String) constant.
	 */
	public static final String ENABLED_DESCRIPTION = "Component enabled";

	/**
	 * Cluster_id and return the String result.
	 * 
	 * @return Results of the cluster_id (<code>String</code>) value.
	 */
	public String cluster_id();

	/**
	 * Enabled and return the boolean result.
	 * 
	 * @return Results of the enabled (<code>boolean</code>) value.
	 */
	public boolean enabled();

	/**
	 * ID and return the String result.
	 * 
	 * @return Results of the ID (<code>String</code>) value.
	 */
	public String id();

}
