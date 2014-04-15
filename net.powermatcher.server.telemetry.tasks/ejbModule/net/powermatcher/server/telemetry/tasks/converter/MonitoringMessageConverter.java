package net.powermatcher.server.telemetry.tasks.converter;


import java.nio.charset.Charset;
import java.util.Date;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.TextMessage;

import net.powermatcher.telemetry.model.data.MonitoringData;

/**
 * @author IBM
 * @version 0.9.0
 */
public class MonitoringMessageConverter {

	/**
	 * Number of elements expected in the message string. 
	 */
	private static int MSG_DATA_ELEMENTS = 7;
	
	/**
	 * Index of 'status' data in the message string. 
	 */
	private static int STATUS_MSG_INDEX = 0;
	/**
	 * Index of 'severity' data in the message string. 
	 */
	private static int SEVERITY_MSG_INDEX = 1;
	/**
	 * Index of 'clusterId' data in the message string. 
	 */
	private static int CLUSTER_ID_MSG_INDEX = 2;
	/**
	 * Index of 'configuration item id'  data in the message string. 
	 */
	private static int CONFIGURATION_ITEM_ID_MSG_INDEX = 3;
	/**
	 * Index of 'configuration item name' data in the message string. 
	 */
	private static int CONFIGURATION_ITEM_NAME_MSG_INDEX = 4;
	/**
	 * Index of 'component name' data in the message string. 
	 */
	private static int COMPONENT_NAME_MSG_INDEX = 5;
	/**
	 * Index of 'server name' data in the message string. 
	 */
	private static int SERVER_NAME_MSG_INDEX = 6;
	
	private static final Charset UTF8 = Charset.forName("UTF8");
	
	
	
	/**
	 * Converts a String message to a MonitoringData instance. The statusDate field
	 * will not be set. The message should be in comma separated format and should
	 * have the following syntax: 
	 * "status,severity,namespace_id,ci_id,ci_name,component_name,server_name" 
	 * 
	 * @param message		The message
	 * @return
	 * @throws Exception
	 */
	public static MonitoringData toData(final String message) throws Exception {
		MonitoringData data = null;

		if (message != null) {
			String[] tokens = message.split(",");

			if (tokens.length == MSG_DATA_ELEMENTS) {
				String clusterId = tokens[CLUSTER_ID_MSG_INDEX];
				String configurationItem = tokens[CONFIGURATION_ITEM_ID_MSG_INDEX];
				String configurationItemName = tokens[CONFIGURATION_ITEM_NAME_MSG_INDEX];
				String componentName = tokens[COMPONENT_NAME_MSG_INDEX];
				String serverName = tokens[SERVER_NAME_MSG_INDEX];
				String status = tokens[STATUS_MSG_INDEX];
				Character severity = tokens[SEVERITY_MSG_INDEX].charAt(0);
	
				data = new MonitoringData(clusterId, configurationItem, configurationItemName, componentName, serverName, status, null, severity);
			}
			else {
				throw new Exception("Unexpected message contents or format. Message: " + message);
			}
		}
		else {
			throw new Exception("Message string is null");
		}
		return data;
	}
	
	/**
	 * Converts a JMS message to a MonitoringData object.
	 * @param message	The message to convert.
	 * @return			The message converted to a MonitoringData object.
	 * @throws Exception
	 */
	public static MonitoringData toData(Message message) throws Exception {
		BytesMessage bytesMsg = null;
    	TextMessage textMsg = null;
    	String msg = null;
		
		if (message instanceof BytesMessage) {
			bytesMsg = (BytesMessage) message;
			byte[] buffer = new byte[(int) bytesMsg.getBodyLength()];
			bytesMsg.readBytes(buffer);
			msg = new String(buffer, UTF8);
		} else {
			textMsg = (TextMessage) message;
			msg = textMsg.getText();
		}
		
		MonitoringData data = MonitoringMessageConverter.toData(msg);
		if (data != null) {
			data.setStatusDate(new Date(message.getJMSTimestamp()));
		}
		
		return data;
	}
}
