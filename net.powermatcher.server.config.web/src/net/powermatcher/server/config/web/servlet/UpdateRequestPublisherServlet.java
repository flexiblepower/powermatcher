package net.powermatcher.server.config.web.servlet;


import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.powermatcher.server.config.web.ConfigConstants;


/**
 * Servlet implementation class TriggerUpdateServlet
 */
public class UpdateRequestPublisherServlet extends HttpServlet {


	/**
	 * 
	 */
	private static final long serialVersionUID = 8174511082481904456L;

	/**
	 * Define the logger (Logger) field.
	 */
	private final static Logger logger = Logger.getLogger(UpdateRequestPublisherServlet.class.getName());
	
	@Resource(name="ConfigurationCF")
	private ConnectionFactory cf;

	@Resource(name="ConfigurationTopic")
	private Topic topic;
	
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public UpdateRequestPublisherServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String errmsg = null;
		String exceptionMsg = null;
		String topicName = null;
		
		// Get parameter
		String nodeId = (String) request.getParameter(ConfigConstants.CONFIG_PARAMETER_NODE_ID);
		
		try {
			String subTopic = ConfigConstants.CONFIG_UPDATE_TOPIC_SUFFIX + '/' + nodeId;
			topicName = createSubtopic(this.topic.getTopicName(), subTopic);
			
			Connection conn = this.cf.createConnection();
			conn.start();
			Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Message m = sess.createTextMessage("Update node configuration: " + nodeId);
			Topic dynamicTopic = sess.createTopic(topicName);
			MessageProducer mp = sess.createProducer(dynamicTopic);
			mp.send(m);
			logger.info("JMS message sent to topic: " + topicName);
		} catch (Exception ex) {
			errmsg = "Failed to send JMS message to topic: " + topicName;
			logger.log(Level.WARNING, errmsg, ex);
			exceptionMsg = ex.getMessage();
			if (ex.getCause() != null) {
				exceptionMsg += "<br>Cause: " + ex.getCause().getMessage();
			}
		}	
		
		// Redirect to error or success page		
		try {
			if (errmsg != null) {
				System.err.println("Failed to sent update trigger message.");
				showErrorPage(request, response, errmsg, exceptionMsg);

			} else {
				System.out.println("Successfully sent update trigger message.");
				showSuccessPage(request, response, "topic name: " + topicName);
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error redirecting to web page.", e);
		}
	}

	/**
	 * Create the implementation-dependent JMS topic name for a subtopic.
	 * @param baseTopic Base topic name, topic://NAME?param1=value1 in WebSphere.
	 * @param subTopic Subtopic to add, without preceding /
	 * @return topic://NAME/subtopic?param1=value1
	 */
	private String createSubtopic(String baseTopic, String subTopic) {
		String topicName;
		int pos = baseTopic.indexOf('?');
		if (pos == -1) {
			topicName = baseTopic + '/' + subTopic;
		} else {
			topicName = baseTopic.substring(0,pos) + '/' + subTopic;
		}
		return topicName;
	}
	
	protected void showErrorPage(HttpServletRequest request,
			HttpServletResponse response, String errorMessage, String stackTrace)
			throws ServletException, IOException {

		request.setAttribute("config.web.errormsg", errorMessage);
		request.setAttribute("config.web.exception", stackTrace);

		getServletConfig().getServletContext()
				.getRequestDispatcher("/error.jsp")
				.forward(request, response);
	}
	
	protected void showSuccessPage(HttpServletRequest request,
			HttpServletResponse response, String message)
			throws ServletException, IOException {

		request.setAttribute("config.web.message", message);

		getServletConfig().getServletContext()
				.getRequestDispatcher("/success.jsp")
				.forward(request, response);
	}
}
