package net.powermatcher.server.telemetry.tasks;


import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.NamingException;

import net.powermatcher.server.telemetry.database.dao.EventDataAccess;
import net.powermatcher.server.telemetry.tasks.converter.MonitoringMessageConverter;
import net.powermatcher.telemetry.model.data.MonitoringData;


/**
 * @author IBM
 * @version 0.9.0
 * 
 * Message-Driven Bean implementation class for: MonitoringDataProcessorBean
 *
 */
@MessageDriven(
		activationConfig = { @ActivationConfigProperty(
				propertyName = "destinationType", propertyValue = "javax.jms.Topic"
		) }, 
		mappedName = "Status")
public class MonitoringDataProcessorBean implements MessageListener {
	/**
	 * Define the logger (Logger) field.
	 */
	protected final static Logger logger = Logger.getLogger(MonitoringDataProcessorBean.class.getName());

	
    /**
     * Default constructor. 
     */
    public MonitoringDataProcessorBean() {

    }
	
	/**
     * @see MessageListener#onMessage(Message)
     */
    public void onMessage(Message message) {

    	try {
    		logger.fine("Message received: " + message);
			MonitoringData mData = MonitoringMessageConverter.toData(message);
    		logger.info("Status message received: " + mData);

    		handleMonitoringDataMessage(mData);
    		
    	} catch (JMSException  e) {
    		logger.severe("Handling message failed. " + e.getErrorCode() + " " + e.getMessage() + " Message: " + message.toString());
			e.printStackTrace();
		} catch (Exception e) {
    		logger.severe("Handling message failed. " + e.getMessage() + " Message: " + message.toString());
			e.printStackTrace();
		}   	        
    }

	private void handleMonitoringDataMessage(MonitoringData mData) throws Exception {
		String dataSourceName = getDataSourceName(mData.getClusterId());
		if (dataSourceName == null) {
			logger.severe("Data source could not be determined. Could not handle Status Data message.");
		} else {
			EventDataAccess dataAccess = EventDataAccess.getInstance(dataSourceName);
			if (dataAccess == null) {
				logger.severe("No data source " + dataSourceName + " found");
			} else {
				dataAccess.addMonitoringData(mData);
			}
		}
	}

	private String getDataSourceName(String clusterId) throws NamingException {
		String dataSourceName = null;
		if (clusterId != null)  {
			dataSourceName = "jdbc/" + clusterId;
		}	 
		return dataSourceName;
	}
}
