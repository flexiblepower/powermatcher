package net.powermatcher.server.event.tasks;


import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.naming.NamingException;

import net.powermatcher.server.event.database.dao.PwmEventDataAccess;

import net.powermatcher.core.messaging.protocol.adapter.log.AbstractLogMessage.MessageType;
import net.powermatcher.core.messaging.protocol.adapter.log.BidLogMessage;
import net.powermatcher.core.messaging.protocol.adapter.log.PriceLogMessage;

/**
 * Message-Driven Bean implementation class for: PowermatcherLogDataMdb
 *
 */
@MessageDriven(
		activationConfig = { @ActivationConfigProperty(
				propertyName = "destinationType", propertyValue = "javax.jms.Topic"
		) }, 
		mappedName = "PowerMatcherTopicName")
public class PowermatcherLogDataMdb implements MessageListener {
	
	/**
	 * Define the logger (Logger) field.
	 */
	protected final static Logger logger = Logger.getLogger(PowermatcherLogDataMdb.class.getName());
	
    /**
     * Default constructor. 
     */
    public PowermatcherLogDataMdb() {

    }
	
	/**
     * @see MessageListener#onMessage(Message)
     */
    public void onMessage(Message message) {
		BytesMessage bytesMsg = null;

		try {
			if (message instanceof BytesMessage) {
				bytesMsg = (BytesMessage) message;
				byte[] buffer = new byte[(int) bytesMsg.getBodyLength()];
				bytesMsg.readBytes(buffer);
				int msgType = buffer.length >= 2 ? buffer[1] : MessageType.UNDEFINED.ordinal(); 
				if (msgType == MessageType.BID.ordinal()) {
					BidLogMessage bidLogMessage = new BidLogMessage(buffer);
					logger.info("Bid log message received: " + bidLogMessage.toString());
					handleBidLogMessage(bidLogMessage);
				} else if (msgType == MessageType.PRICE.ordinal()) {
					PriceLogMessage priceLogMessage = new PriceLogMessage( buffer);
					logger.info("Price log message received: " + priceLogMessage.toString());
					handlePriceLogMessage(priceLogMessage);
				} else {
					logger.info("Unexpected message type: " + msgType + "\nIgnoring message: \n" + message.toString());
				}
			} else {
				logger.info("Unexpected message type received. Type: "
						+ "\nIgnoring message: \n" + message.toString());
				return;
			}
		} catch (JMSException e) {
			logger.severe("Handling message failed. " + e.getErrorCode() + " "
					+ e.getMessage());
		} catch (Exception e) {
			logger.severe("Handling message failed. " + e.getMessage());
			e.printStackTrace();
		}
    }
    
	protected void handleBidLogMessage(BidLogMessage message) throws Exception {
		String clusterId = message.getBidLogInfo().getClusterId();
		String dataSourceName = getDataSourceName(clusterId);
		if (dataSourceName == null) {
			logger.severe("Data source could not be determined. Could not handle Telemetry Data message.");
		} else {
			PwmEventDataAccess dataAccess = PwmEventDataAccess.getInstance(dataSourceName);
			if (dataAccess == null) {
				logger.severe("No data source " + dataSourceName + " found");
			} else {
				dataAccess.addBidInfoData(message);

			}
		}
	}

	protected void handlePriceLogMessage(PriceLogMessage message) throws Exception {
		String clusterId = message.getPriceLogInfo().getClusterId();
		String dataSourceName = getDataSourceName(clusterId);
		if (dataSourceName == null) {
			logger.severe("Data source could not be determined. Could not handle Telemetry Data message.");
		} else {
			PwmEventDataAccess dataAccess = PwmEventDataAccess.getInstance(dataSourceName);
			if (dataAccess == null) {
				logger.severe("No data source " + dataSourceName + " found");
			} else {
				dataAccess.addPriceInfoData(message);
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
