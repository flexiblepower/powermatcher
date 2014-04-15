package net.powermatcher.server.config.jpa.entity.controller;


import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import net.powermatcher.server.config.jpa.entity.Configuration;
import net.powermatcher.server.config.jpa.entity.Nodeconfiguration;

import com.ibm.jpa.web.Action;
import com.ibm.jpa.web.JPAManager;

@JPAManager(targetEntity = net.powermatcher.server.config.jpa.entity.Nodeconfiguration.class)
public class NodeconfigurationManager {

	private EntityManagerFactory emf;

	public NodeconfigurationManager() {
	
	}

	public NodeconfigurationManager(EntityManagerFactory emf) {
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
	public String createNodeconfiguration(Nodeconfiguration nodeconfiguration)
			throws Exception {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(nodeconfiguration);
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
	public String deleteNodeconfiguration(Nodeconfiguration nodeconfiguration)
			throws Exception {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			nodeconfiguration = em.merge(nodeconfiguration);
			em.remove(nodeconfiguration);
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
	public String updateNodeconfiguration(Nodeconfiguration nodeconfiguration)
			throws Exception {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			nodeconfiguration = em.merge(nodeconfiguration);
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
	public Nodeconfiguration findNodeconfigurationByNodeid(String nodeid) {
		Nodeconfiguration nodeconfiguration = null;
		EntityManager em = getEntityManager();
		try {
			nodeconfiguration = (Nodeconfiguration) em.find(
					Nodeconfiguration.class, nodeid);
			
			// Trick to get load all the configurations
			if (nodeconfiguration != null) {
				List<Configuration> configurationList = nodeconfiguration.getConfigurationList();
				if (configurationList != null && configurationList.size() > 0) {
					for (Configuration c : configurationList) {
						retrieveConfigurations(c);
						retrieveParentConfiguration(c);
					}
				}
			}
		} finally {
			em.close();
		}
		return nodeconfiguration;
	}

	private void retrieveParentConfiguration(Configuration configuration) {
		Configuration parent = configuration.getParent();
		if (parent != null) {
			retrieveParentConfiguration(parent);
		}
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
	public Nodeconfiguration getNewNodeconfiguration() {
	
		Nodeconfiguration nodeconfiguration = new Nodeconfiguration();
	
		return nodeconfiguration;
	}

}