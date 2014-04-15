package net.powermatcher.core.config;


import java.util.HashSet;
import java.util.Set;

/**
 * @author IBM
 * @version 0.9.0
 */
public class ConfigurationSpecUtil {
	/**
	 * Configuration set with the specified hierarchy set parameter and return
	 * the Set<ConfigurationSpec> result.
	 * 
	 * @param hierarchySet
	 *            The hierarchy set (<code>Set<ConfigurationSpec></code>)
	 *            parameter.
	 * @return the set of configurations of all configurations in the hierarchy.
	 *         The hierarchy will not be preserved, all relations to the parent
	 *         configuration groups will be set to null.
	 */
	public static Set<ConfigurationSpec> configurationSet(final Set<ConfigurationSpec> hierarchySet) {
		Set<ConfigurationSpec> all = new HashSet<ConfigurationSpec>();

		for (ConfigurationSpec c : hierarchySet) {
			all.addAll(c.configurationSet());
		}
		return all;
	}

}
