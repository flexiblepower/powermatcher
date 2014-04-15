package net.powermatcher.telemetry.model.data;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author IBM
 * @version 0.9.0
 */
public abstract class Data {
	/**
	 * Get index with the specified name parameter and return the int result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the get index (<code>int</code>) value.
	 */
	private static int getIndex(final String name) {
		return Integer.parseInt(name.substring(name.lastIndexOf('_') + 1));
	}

	/**
	 * Define the data map (Map) field.
	 */
	protected Map<String, Object> dataMap;

	/**
	 * Constructs an instance of this class.
	 * 
	 * @see #Data(Map)
	 */
	protected Data() {
		this.dataMap = new HashMap<String, Object>();
	}

	/**
	 * Constructs an instance of this class from the specified data map
	 * parameter.
	 * 
	 * @param dataMap
	 *            The data map (<code>Map<String,Object></code>) parameter.
	 * @see #Data()
	 */
	protected Data(final Map<String, Object> dataMap) {
		this.dataMap = dataMap;
	}

	/**
	 * Add child data with the child's data key.
	 * 
	 * @param data
	 *            The generic data (<code>Data</code>) parameter.
	 */
	public void addData(final Data data) {
		Map<String, Object> map = data.getDataMap();
		this.dataMap.put(data.getKey() + '_' + this.dataMap.size(), map);
	}

	/**
	 * Get child keys with the specified prefix parameter and return the
	 * String[] result.
	 * 
	 * @param prefix
	 *            The prefix (<code>String</code>) parameter.
	 * @return Results of the get child keys (<code>String[]</code>) value.
	 */
	protected String[] getChildKeys(final String prefix) {
		int count = 0;
		String list[] = new String[this.dataMap.size()];
		for (Iterator<Map.Entry<String, Object>> iterator = this.dataMap.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<String, Object> entry = iterator.next();
			String key = entry.getKey();
			if (key.startsWith(prefix) && entry.getValue() instanceof Map) {
				list[getIndex(key)] = key;
				count += 1;
			}
		}
		String result[] = new String[count];
		count = 0;
		for (int i = 0; i < list.length; i++) {
			String key = list[i];
			if (key != null) {
				result[count++] = key.substring(0, key.lastIndexOf('_'));
			}
		}
		return result;
	}

	/**
	 * Get children with the specified prefix parameter and return the
	 * Map<String,Object>[] result.
	 * 
	 * @param prefix
	 *            The prefix (<code>String</code>) parameter.
	 * @return Results of the get children (<code>Map<String,Object>[]</code>)
	 *         value.
	 */
	@SuppressWarnings("unchecked")
	protected Map<String, Object>[] getChildren(final String prefix) {
		int count = 0;
		Map.Entry<String, Object> list[] = new Map.Entry[this.dataMap.size()];
		for (Iterator<Map.Entry<String, Object>> iterator = this.dataMap.entrySet().iterator(); iterator.hasNext();) {
			Map.Entry<String, Object> entry = iterator.next();
			String key = entry.getKey();
			if (key.startsWith(prefix) && entry.getValue() instanceof Map) {
				list[getIndex(key)] = entry;
				count += 1;
			}
		}
		Map<String, Object> result[] = (new Map[count]);
		count = 0;
		for (int i = 0; i < list.length; i++) {
			Map.Entry<String, Object> entry = list[i];
			if (entry != null) {
				Map<String, Object> m = (Map<String, Object>) entry.getValue();
				result[count++] = m;
			}
		}
		return result;
	}

	/**
	 * Gets the data map (Map) value.
	 * 
	 * @return The data map (<code>Map<String,Object></code>) value.
	 */
	public Map<String, Object> getDataMap() {
		return this.dataMap;
	}

	/**
	 * Gets the key (String) value for the data type.
	 * 
	 * @return The data type key (<code>String</code>).
	 */
	public abstract String getKey();

}
