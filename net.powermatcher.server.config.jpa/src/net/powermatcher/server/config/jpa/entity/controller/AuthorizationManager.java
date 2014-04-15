package net.powermatcher.server.config.jpa.entity.controller;


import com.ibm.jpa.web.JPAManager;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import com.ibm.jpa.web.NamedQueryTarget;
import com.ibm.jpa.web.Action;
import java.util.List;
import javax.persistence.Query;

import net.powermatcher.server.config.jpa.entity.Authorization;
import net.powermatcher.server.config.jpa.entity.AuthorizationPK;

@SuppressWarnings("unchecked")
@JPAManager(targetEntity = net.powermatcher.server.config.jpa.entity.Authorization.class)
public class AuthorizationManager {

	private EntityManagerFactory emf;

	public AuthorizationManager() {
	
	}

	public AuthorizationManager(EntityManagerFactory emf) {
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
	public String createAuthorization(Authorization authorization)
			throws Exception {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			em.persist(authorization);
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
	public String deleteAuthorization(Authorization authorization)
			throws Exception {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			authorization = em.merge(authorization);
			em.remove(authorization);
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
	public String updateAuthorization(Authorization authorization)
			throws Exception {
		EntityManager em = getEntityManager();
		try {
			em.getTransaction().begin();
			authorization = em.merge(authorization);
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
	public Authorization findAuthorizationByPrimaryKey(String userid,
			String nodeid) {
		Authorization authorization = null;
		EntityManager em = getEntityManager();
		AuthorizationPK pk = new AuthorizationPK();
		pk.setUserid(userid);
		pk.setNodeid(nodeid);
		try {
			authorization = (Authorization) em.find(Authorization.class, pk);
		} finally {
			em.close();
		}
		return authorization;
	}

	@Action(Action.ACTION_TYPE.NEW)
	public Authorization getNewAuthorization() {
	
		Authorization authorization = new Authorization();
	
		AuthorizationPK id = new AuthorizationPK();
		authorization.setId(id);
	
		return authorization;
	}

	@NamedQueryTarget("getAuthorizationByNodeid")
	public List<Authorization> getAuthorizationByNodeid(String id_nodeid) {
		EntityManager em = getEntityManager();
		List<Authorization> results = null;
		try {
			Query query = em.createNamedQuery("getAuthorizationByNodeid");
			query.setParameter("id_nodeid", id_nodeid);
			results = (List<Authorization>) query.getResultList();
		} finally {
			em.close();
		}
		return results;
	}

	@NamedQueryTarget("getAuthorizationByUserid")
	public List<Authorization> getAuthorizationByUserid(String id_userid) {
		EntityManager em = getEntityManager();
		List<Authorization> results = null;
		try {
			Query query = em.createNamedQuery("getAuthorizationByUserid");
			query.setParameter("id_userid", id_userid);
			results = (List<Authorization>) query.getResultList();
		} finally {
			em.close();
		}
		return results;
	}

	@NamedQueryTarget("getAuthorization")
	public List<Authorization> getAuthorization() {
		EntityManager em = getEntityManager();
		List<Authorization> results = null;
		try {
			Query query = em.createNamedQuery("getAuthorization");
			results = (List<Authorization>) query.getResultList();
		} finally {
			em.close();
		}
		return results;
	}

}