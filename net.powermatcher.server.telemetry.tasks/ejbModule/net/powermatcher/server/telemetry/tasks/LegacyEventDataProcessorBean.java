package net.powermatcher.server.telemetry.tasks;


import java.nio.charset.Charset;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.naming.NamingException;

import net.powermatcher.server.telemetry.database.dao.EventDataAccess;
import net.powermatcher.telemetry.model.converter.XMLConverter;
import net.powermatcher.telemetry.model.data.TelemetryData;


/**
 * @author IBM
 * @version 0.9.0
 * 
 * Message-Driven Bean implementation class for: LegacyEventDataProcessorBean
 * Processes telemetry messages on the legacy CPSS topic for backwards compatibility.
 *
 */
@MessageDriven(
		activationConfig = { @ActivationConfigProperty(
				propertyName = "destinationType", propertyValue = "javax.jms.Topic"
		) }, 
		mappedName = "CPSS")
public class LegacyEventDataProcessorBean implements MessageListener {

	/**
	 * Define the logger (Logger) field.
	 */
	protected final static Logger logger = Logger.getLogger(LegacyEventDataProcessorBean.class.getName());
	private static final Charset UTF8 = Charset.forName("UTF8");
	
    /**
     * Default constructor. 
     */
    public LegacyEventDataProcessorBean() {

    }
	
	/**
     * @see MessageListener#onMessage(Message)
     */
    public void onMessage(Message message) {
		BytesMessage bytesMsg = null;
    	TextMessage textMsg = null;
    	String msg = null;
    	
    	try {
			if (message instanceof BytesMessage) {
				bytesMsg = (BytesMessage) message;
				byte[] buffer = new byte[(int) bytesMsg.getBodyLength()];
				bytesMsg.readBytes(buffer);
				msg = new String(buffer, UTF8);
			} else {
				textMsg = (TextMessage) message;
				msg = textMsg.getText();
			}
    		logger.fine("Message received: " + message);
    		logger.fine("Message body: " + msg);
    		
    		TelemetryData telemetryData = XMLConverter.toData(msg);
    		handleTelemetryDataMessage(telemetryData);
    		
    	} catch (JMSException  e) {
    		logger.severe("Handling message failed. " + e.getErrorCode() + " " + e.getMessage());
		} catch (Exception e) {
    		logger.severe("Handling message failed. " + e.getMessage());
			e.printStackTrace();
		}   	
    }
    
	protected void handleTelemetryDataMessage(TelemetryData message) throws Exception {

		String dataSourceName = getDataSourceName(message);
		if (dataSourceName == null) {
			logger.severe("Data source could not be determined. Could not handle Telemetry Data message.");
		} else {
			EventDataAccess dataAccess = EventDataAccess.getInstance(dataSourceName);
			if (dataAccess == null) {
				logger.severe("No data source " + dataSourceName + " found");
			} else {
				dataAccess.addMeasurementData(message);
				dataAccess.addStatusData(message);
				dataAccess.addAlertData(message);
				dataAccess.addControlData(message);
				dataAccess.addRequestData(message);
				dataAccess.addResponseData(message);
			}
		}
	}
	
	private String getDataSourceName(TelemetryData message) throws NamingException {
		String clusterId = message.getClusterId();
		String dataSourceName = null;
		if (clusterId != null)  {
			dataSourceName = "jdbc/" + clusterId;
		}	 
		return dataSourceName;
	}
}
