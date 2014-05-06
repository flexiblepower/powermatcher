package net.powermatcher.server.telemetry.tasks.test;


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

import net.powermatcher.telemetry.model.converter.XMLConverter;
import net.powermatcher.telemetry.model.data.TelemetryData;

/**
 * @author IBM
 * @version 0.9.0
 * 
 * Servlet implementation class StatusPublisherServlet
 */
public class StatusPublisherServlet extends HttpServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2117703216264566996L;

	/**
	 * Define the logger (Logger) field.
	 */
	private final static Logger logger = Logger.getLogger(TelemetryPublisherServlet.class.getName());
	
	@Resource(name="TelemetryCF")
	private ConnectionFactory cf;

	@Resource(name="StatusTopic")
	private Topic topic;

    /**
     * @see HttpServlet#HttpServlet()
     */
    public StatusPublisherServlet() {

		
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String errmsg = null;
		String exceptionMsg = null;
		String topicName = null;
		
		// Get parameter
		String messageText = (String) request.getParameter("messageText");
		
			try {
	    		topicName = this.topic.getTopicName();
				//topicName = "Status/test";
	    		if (messageText == null || messageText.isEmpty()) {
	    			errmsg = "No or empty message text specified in request.";
	    		} else {
					TelemetryData telemetryData = XMLConverter.toData(messageText);
					Connection conn = this.cf.createConnection();
		    		conn.start();
		    		Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
		    		Message m = sess.createTextMessage(messageText);
		    		Topic dynamicTopic = sess.createTopic(createSubtopic(topicName, telemetryData.getClusterId()));
		    		MessageProducer mp = sess.createProducer(dynamicTopic);
		    		mp.send(m);
		    		logger.info("JMS message sent to topic: " +  topicName);
	    		}
	   		} catch(Exception ex) {
	    		errmsg = "Failed to send JMS message to topic: " + topicName;
	    		logger.log(Level.WARNING, errmsg, ex);
	   		}				
		
		// Redirect to error or success page		
		try {
			if (errmsg != null) {
				showErrorPage(request, response, errmsg, exceptionMsg);

			} else {
				showSuccessPage(request, response, messageText);
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

		request.setAttribute("sensorevent.client.jmstest.errormsg", errorMessage);
		request.setAttribute("sensorevent.client.jmstest.exception", stackTrace);

		getServletConfig().getServletContext()
				.getRequestDispatcher("/error.jsp")
				.forward(request, response);
	}
	
	protected void showSuccessPage(HttpServletRequest request,
			HttpServletResponse response, String message)
			throws ServletException, IOException {

		request.setAttribute("sensorevent.client.jmstest.message", message);

		getServletConfig().getServletContext()
				.getRequestDispatcher("/success.jsp")
				.forward(request, response);
	}
}
