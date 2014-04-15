package net.powermatcher.server.config.auth;


import javax.persistence.EntityManagerFactory;

import net.powermatcher.server.config.jpa.entity.Authorization;
import net.powermatcher.server.config.jpa.entity.controller.AuthorizationManager;


public class DbAuthorization implements ConfigAuthorizationService {

	private EntityManagerFactory emf;
	
	public DbAuthorization(EntityManagerFactory emf) {
		super();
		this.emf = emf;
	}

	@Override
	public boolean isAuthorized(String userid, String nodeid) {
		AuthorizationManager authManager = new AuthorizationManager(emf);
		Authorization auth = authManager.findAuthorizationByPrimaryKey(userid, nodeid);
		
		return (auth != null);
	}

}
