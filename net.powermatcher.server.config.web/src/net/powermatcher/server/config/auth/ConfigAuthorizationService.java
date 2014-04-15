package net.powermatcher.server.config.auth;


public interface ConfigAuthorizationService {
	
	public boolean isAuthorized(String userid, String nodeid);

}
