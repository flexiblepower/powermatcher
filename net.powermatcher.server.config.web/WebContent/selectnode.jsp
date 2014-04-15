<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@page import="net.powermatcher.server.config.jpa.entity.Authorization"%>
<%@page import="java.util.List"%>
<%@page import="net.powermatcher.server.config.jpa.entity.controller.AuthorizationManager"%>
<%@page import="net.powermatcher.server.config.jpa.entity.controller.NodeconfigurationManager"%>
<%@page import="javax.persistence.EntityManagerFactory"%>
<%@page import="javax.persistence.PersistenceUnit"%>
<%@page import="net.powermatcher.server.config.web.ConfigConstants"%>

<%@page
	language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%!
// Dependency injection should work for jsp, but somehow doesn't.
// @PersistenceUnit
//	private EntityManagerFactory emf;

	private String[] getNodeIds(HttpServletRequest request) {
		EntityManagerFactory emf = javax.persistence.Persistence.createEntityManagerFactory("net.powermatcher.server.config.jpa");
		AuthorizationManager am = new AuthorizationManager(emf);
		List<Authorization> authorizations = am.getAuthorizationByUserid(request.getRemoteUser());
		String nodeIds[] = new String[authorizations.size()];
		for (int i = 0; i < nodeIds.length; i++) {
			nodeIds[i] = authorizations.get(i).getId().getNodeid();
		}
		return nodeIds;
	}
%>
<html>
<head>
<%
	String nodeIds[] = getNodeIds(request);
%>
<title>Select Configuration Node</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
</head>
<body>
	<form action="nodeconfiguration" method="get">
		<fieldset>			
			<legend>View node configuration</legend>
			Select the node configuration: <br>
			<select name="nodeid">
			 <option value="none">Select</option>  
			    <%	for (int i=0; i < nodeIds.length; i++) {  %>
      				<option value='<%=nodeIds[i]%>'><%=nodeIds[i]%></option>    
      			<% } %>
			</select>
			<input type="submit" value="Get Configuration" >
		</fieldset>	
	</form>
	<form action="sendupdaterequest" method="get">
		<fieldset>
			<legend>Update node configuration</legend>
			Select configuration node to trigger a configuration update of the subscribed configuration management agents:<br>
			<select name="<%=ConfigConstants.CONFIG_PARAMETER_NODE_ID%>">
			 <option value="none">Select</option>  
			    <%	for (int i=0; i < nodeIds.length; i++) {  %>
      				<option value='<%=nodeIds[i]%>'><%=nodeIds[i]%></option>    
      			<% } %>
			</select>
			<input type="submit" value="Trigger Update">
		</fieldset>
	</form>
</body>
</html>