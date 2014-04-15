package net.powermatcher.server.event.database.dao;


import java.util.HashMap;
import java.util.Map;

import javax.naming.InitialContext;
import javax.sql.DataSource;

public class ResourceLocator {

	private static ResourceLocator mySingleton;
	private Map<String, DataSource> dataSources;
	private InitialContext initialContext;
	
	protected ResourceLocator() throws Exception {
		this.dataSources = new HashMap<String, DataSource>();
		this.initialContext = new InitialContext();
	}
	
	public synchronized static ResourceLocator singleton() throws Exception {
		if (mySingleton == null) {
			mySingleton = new ResourceLocator();
		}
		
		return mySingleton;
	}
	
	public synchronized DataSource getDataSource(String dataSourceName) throws Exception {
		DataSource ds = this.dataSources.get(dataSourceName);
		if (ds == null) {
			Object obj = this.initialContext.lookup(dataSourceName);
			if (obj instanceof DataSource) {
				ds = (DataSource) obj;
				this.dataSources.put(dataSourceName, ds);
			}
		}
		
		return ds;
	}
	
}
