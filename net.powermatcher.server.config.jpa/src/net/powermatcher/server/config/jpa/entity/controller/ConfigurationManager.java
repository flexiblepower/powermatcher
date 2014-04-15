package net.powermatcher.server.config.jpa.entity.controller;


import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import net.powermatcher.server.config.jpa.entity.Configuration;
import net.powermatcher.server.config.jpa.entity.ConfigurationPK;

import com.ibm.jpa.web.Action;
import com.ibm.jpa.web.JPAManager;

@JPAManager(targetEntity = net.powermatcher.server.config.jpa.entity.Configuration.class)
public class ConfigurationManager {

	private EntityManagerFactory emf;

	public ConfigurationManager() {
	
	}

	public ConfigurationManager(EntityManagerFactory emf) {
		this.emf = emf;
	}

	public void setEntityManagerFactory(EntityManagerFactory emf) {
		this.emf = emf;
	}

	private EntityManager getEntityManager() {
		if (emf == null) {
			throw new RuntimeException(
					"The EntityManagerFactory is null.  This must be passed in to the constructor or set using the setEntityManagerFactory() method.");
		}
		return emf.createEntityManager();
	}

	@Action(Action.ACTION_TYPE.CREATE)
	public String createConfiguration(Configuration configuration)
			throws Exception {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(configuration);
			em.getTransaction().commit();
		} catch (Exception ex) {
			try {
				if (em.getTransaction().isActive()) {
					em.getTransaction().rollback();
				}
			} catch (Exception e) {
				ex.printStackTrace();
				throw e;
			}
			throw ex;
		} finally {
			em.close();
		}
		return "";
	}

	@Action(Action.ACTION_TYPE.DELETE)
	public String deleteConfiguration(Configuration configuration)
			throws Exception {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			configuration = em.merge(configuration);
			em.remove(configuration);
			em.getTransaction().commit();
		} catch (Exception ex) {
			try {
				if (em.getTransaction().isActive()) {
					em.getTransaction().rollback();
				}
			} catch (Exception e) {
				ex.printStackTrace();
				throw e;
			}
			throw ex;
		} finally {
			em.close();
		}
		return "";
	}

	@Action(Action.ACTION_TYPE.UPDATE)
	public String updateConfiguration(Configuration configuration)
			throws Exception {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			configuration = em.merge(configuration);
			em.getTransaction().commit();
		} catch (Exception ex) {
			try {
				if (em.getTransaction().isActive()) {
					em.getTransaction().rollback();
				}
			} catch (Exception e) {
				ex.printStackTrace();
				throw e;
			}
			throw ex;
		} finally {
			em.close();
		}
		return "";
	}

	@Action(Action.ACTION_TYPE.FIND)
	public Configuration findConfigurationByPrimaryKey(String clusterId,
			String configId) {
		Configuration configuration = null;
		EntityManager em = getEntityManager();
		ConfigurationPK pk = new ConfigurationPK();
		pk.setClusterId(clusterId);
		pk.setConfigId(configId);
		try {
			configuration = (Configuration) em.find(Configuration.class, pk);
			retrieveConfigurations(configuration);
		} finally {
			em.close();
		}
		return configuration;
	}
	
	private void retrieveConfigurations(Configuration configuration) {
		Set<Configuration> configurationSet = configuration.getConfigurations();
		if (configurationSet != null && configurationSet.size() > 0) {
			for (Configuration c : configurationSet) {
				retrieveConfigurations(c);
			}
		}
	}

	@Action(Action.ACTION_TYPE.NEW)
	public Configuration getNewConfiguration() {
	
		Configuration configuration = new Configuration();
	
		ConfigurationPK id = new ConfigurationPK();
		configuration.setId(id);
	
		return configuration;
	}

}