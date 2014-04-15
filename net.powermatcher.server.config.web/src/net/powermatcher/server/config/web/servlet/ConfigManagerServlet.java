package net.powermatcher.server.config.web.servlet;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.powermatcher.server.config.auth.ConfigAuthorizationService;
import net.powermatcher.server.config.auth.DbAuthorization;
import net.powermatcher.server.config.jpa.entity.Nodeconfiguration;
import net.powermatcher.server.config.jpa.entity.controller.NodeconfigurationManager;
import net.powermatcher.server.config.web.ConfigConstants;
import net.powermatcher.server.config.xml.writer.NodeConfigurationXmlWriter;


/**
 * Servlet implementation class ConfigManagerServlet
 */
public class ConfigManagerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	/**
	 * Define the logger (Logger) field.
	 */
	protected final static Logger logger = Logger.getLogger(ConfigManagerServlet.class.getName());

	
	@PersistenceUnit
	private EntityManagerFactory emf;

	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ConfigManagerServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		getResponse(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		getResponse(request, response);
	}

	private void getResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.setContentType(ConfigConstants.CONFIG_SERVICE_MIME_TYPE);
	    PrintWriter out = response.getWriter();

		// Get parameter
		String nodeId = (String) request.getParameter(ConfigConstants.CONFIG_PARAMETER_NODE_ID);
		logger.info("Configuration request received for node '" + nodeId + "'.");

		// Check authorization
	    String username = request.getRemoteUser();
	    if (!isAuthorized(username, nodeId)) {
	    	// Log error and send error response
			logger.info("Authorization failed for '" + username + "'");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	    	
	    }
	    else {
			Nodeconfiguration nodeconfig = getNodeConfiguration(nodeId);
			if (nodeconfig != null) {
				NodeConfigurationXmlWriter ncw = new NodeConfigurationXmlWriter();
				ncw.setNodeconfig(nodeconfig);
				ncw.write(out);
			} else {
				// Log error and send error response
				logger.info("Could not find requested node configuration in database for node id '"
						+ nodeId + "'");
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
	    }
	}
	
	protected boolean isAuthorized(String username, String nodeid) {
		ConfigAuthorizationService authService = new DbAuthorization(emf);
		return authService.isAuthorized(username, nodeid);
	}
	
	private Nodeconfiguration getNodeConfiguration(String nodeid) {
		Nodeconfiguration nodeconfig = null;
		
		NodeconfigurationManager ncm = new NodeconfigurationManager(emf);
		nodeconfig = ncm.findNodeconfigurationByNodeid(nodeid);
		
		return nodeconfig;
		
	}
}
 