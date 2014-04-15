package net.powermatcher.server.config.jpa.entity.controller;


import com.ibm.jpa.web.JPAManager;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import net.powermatcher.server.config.jpa.entity.Property;
import net.powermatcher.server.config.jpa.entity.PropertyPK;

import com.ibm.jpa.web.Action;

@SuppressWarnings("unchecked")
@JPAManager(targetEntity = net.powermatcher.server.config.jpa.entity.Property.class)
public class PropertyManager {

	private EntityManagerFactory emf;

	public PropertyManager() {
	
	}

	public PropertyManager(EntityManagerFactory emf) {
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
	public String createProperty(Property property) throws Exception {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(property);
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
	public String deleteProperty(Property property) throws Exception {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			property = em.merge(property);
			em.remove(property);
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
	public String updateProperty(Property property) throws Exception {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			property = em.merge(property);
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
	public Property findPropertyByPrimaryKey(String clusterId, String configId,
			String name) {
		Property property = null;
		EntityManager em = getEntityManager();
		PropertyPK pk = new PropertyPK();
		pk.setClusterId(clusterId);
		pk.setConfigId(configId);
		pk.setName(name);
		try {
			property = (Property) em.find(Property.class, pk);
		} finally {
			em.close();
		}
		return property;
	}

	@Action(Action.ACTION_TYPE.NEW)
	public Property getNewProperty() {
	
		Property property = new Property();
	
		PropertyPK id = new PropertyPK();
		property.setId(id);
	
		return property;
	}

}