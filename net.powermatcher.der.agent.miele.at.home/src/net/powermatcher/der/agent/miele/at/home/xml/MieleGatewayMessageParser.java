package net.powermatcher.der.agent.miele.at.home.xml;


import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.powermatcher.der.agent.miele.at.home.gateway.constants.MieleApplianceConstants;
import net.powermatcher.der.agent.miele.at.home.gateway.constants.MieleGatewayConstants;
import net.powermatcher.der.agent.miele.at.home.gateway.constants.MieleGatewayXmlConstants;
import net.powermatcher.der.agent.miele.at.home.gateway.utils.MieleApplianceUtil;
import net.powermatcher.der.agent.miele.at.home.msg.MieleApplianceInfoMessage;
import net.powermatcher.der.agent.miele.at.home.msg.MieleApplianceListMessage;
import net.powermatcher.der.agent.miele.at.home.msg.MieleDishWasherInfoMessage;
import net.powermatcher.der.agent.miele.at.home.msg.MieleFridgeFreezerInfoMessage;
import net.powermatcher.der.agent.miele.at.home.msg.MieleGatewayActionOkMessage;
import net.powermatcher.der.agent.miele.at.home.msg.MieleGatewayErrorMessage;
import net.powermatcher.der.agent.miele.at.home.msg.MieleGatewayMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * @author IBM
 * @version 0.9.0
 */
public class MieleGatewayMessageParser extends DefaultHandler {

	// Initialize logging
	private final static Logger logger = LoggerFactory.getLogger(MieleGatewayMessageParser.class);

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(final String[] args) throws Exception {
		// TODO: Move this to a JUnit test class
		String file1 = "xml/example_dw_info.xml";
		String file2 = "xml/example_fridgefreezer_info.xml";
		String file3 = "xml/example_action_ok.xml";
		String file4 = "xml/example_error.xml";
		String file5 = "xml/example_appliance_list.xml";

		MieleGatewayMessageParser parser = new MieleGatewayMessageParser();

		MieleGatewayMessage msg = parser.parse(new BufferedInputStream(new FileInputStream(file1)),
				MieleGatewayConstants.LANGUAGE_GERMAN);
		System.out.println("Message " + msg.getClass().toString());

		msg = parser.parse(new BufferedInputStream(new FileInputStream(file2)), MieleGatewayConstants.LANGUAGE_GERMAN);
		System.out.println("Message " + msg.getClass().toString());

		msg = parser.parse(new BufferedInputStream(new FileInputStream(file3)), MieleGatewayConstants.LANGUAGE_GERMAN);
		System.out.println("Message " + msg.getClass().toString());

		msg = parser.parse(new BufferedInputStream(new FileInputStream(file4)), MieleGatewayConstants.LANGUAGE_GERMAN);
		System.out.println("Message " + msg.getClass().toString());

		msg = parser.parse(new BufferedInputStream(new FileInputStream(file5)), MieleGatewayConstants.LANGUAGE_GERMAN);
		System.out.println("Message " + msg.getClass().toString());

	}

	// Fields
	private String language;

	// The message
	private MieleGatewayMessage message;

	// Temporary
	private MieleApplianceInfoMessage tempApplianceMessage;
	// Temporary storage for context
	private String tempVal;

	private Map<String, String> tempActionMap;

	@Override
	public void characters(final char[] ch, final int start, final int length) throws SAXException {
		this.tempVal = new String(ch, start, length);
	}

	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {
		if (qName.equalsIgnoreCase(MieleGatewayXmlConstants.ELEMENT_INFORMATION)) {
			// tempApplianceInfo.setInformation(tempInformationMap);
		} else if (qName.equalsIgnoreCase(MieleGatewayXmlConstants.ELEMENT_ACTIONS)) {
			this.tempApplianceMessage.setActions(this.tempActionMap);
		} else if (qName.equalsIgnoreCase(MieleGatewayXmlConstants.ELEMENT_DEVICE)) {
			if (this.message instanceof MieleApplianceListMessage) {
				((MieleApplianceListMessage) this.message).addAppliance(this.tempApplianceMessage);
			} else {
				this.message = this.tempApplianceMessage;
			}
		} else if (qName.equalsIgnoreCase(MieleGatewayXmlConstants.ELEMENT_ACTION)) {
			if (this.message instanceof MieleGatewayActionOkMessage) {
				((MieleGatewayActionOkMessage) this.message).setAction(this.tempVal);
			}
		} else if (qName.equalsIgnoreCase(MieleGatewayXmlConstants.ELEMENT_OK)) {
			// Do nothing
		} else if (qName.equalsIgnoreCase(MieleGatewayXmlConstants.ELEMENT_ERROR)) {
			// Do nothing
		} else if (qName.equalsIgnoreCase(MieleGatewayXmlConstants.ELEMENT_MESSAGE)) {
			if (this.message instanceof MieleGatewayErrorMessage) {
				((MieleGatewayErrorMessage) this.message).setMessage(this.tempVal);
			}
		}
	}

	/**
	 * @param is
	 * @param language
	 * @return TODO
	 * @throws MieleGatewayMessageParserException
	 */
	public MieleGatewayMessage parse(final InputStream is, final String language) throws MieleGatewayMessageParserException {
		// Set the language
		this.language = language;

		// Initialise
		this.message = null;

		// Get a factory
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		spf.setValidating(false);

		try {
			// Get a new instance of parser
			SAXParser sp = spf.newSAXParser();

			// Parse the file and also register this class for call backs
			sp.parse(is, this);

			// Return the parsed message
			return this.message;

		} catch (SAXException se) {
			String msg = "Error occurred while parsing the message from the inputstream. " + se.getMessage();
			logger.error(msg, se);
			throw new MieleGatewayMessageParserException(msg, se);
		} catch (ParserConfigurationException pce) {
			String msg = "Parsing configuration error while parsing input stream";
			logger.error(msg, pce);
			throw new MieleGatewayMessageParserException(msg, pce);
		} catch (IOException ie) {
			String msg = "Parsing message failed. I/O exception while parsing input stream. ";
			logger.error(msg, ie);
			throw new MieleGatewayMessageParserException(msg, ie);
		} catch (Exception e) {
			String msg = "Parsing message failed. Unexpected exception occurred.";
			logger.error(msg, e);
			throw new MieleGatewayMessageParserException(msg, e);
		}

	}

	private void parseKeyValue(final String key, final String value) throws SAXException {
		int property = MieleApplianceUtil.getInformationProperty(key, this.language);
		switch (property) {
		case MieleApplianceConstants.APPLIANCE_CLASS:
			this.tempApplianceMessage.setApplianceClass(MieleApplianceUtil.getApplianceClass(this.language, value));
			break;
		case MieleApplianceConstants.APPLIANCE_STATE:
			this.tempApplianceMessage.setApplianceState(MieleApplianceUtil.getState(this.language, value));
			break;
		case MieleApplianceConstants.REFRIGERATOR_STATE:
			// Downcast using constructor
			if (!(this.tempApplianceMessage instanceof MieleFridgeFreezerInfoMessage)) {
				this.tempApplianceMessage = new MieleFridgeFreezerInfoMessage(this.tempApplianceMessage);
			}
			((MieleFridgeFreezerInfoMessage) this.tempApplianceMessage).setRefrigeratorState(MieleApplianceUtil.getState(
					this.language, value));
			break;
		case MieleApplianceConstants.REFRIGERATOR_TARGET_TEMPERATURE:
			// Downcast using constructor
			if (!(this.tempApplianceMessage instanceof MieleFridgeFreezerInfoMessage)) {
				this.tempApplianceMessage = new MieleFridgeFreezerInfoMessage(this.tempApplianceMessage);
			}
			((MieleFridgeFreezerInfoMessage) this.tempApplianceMessage)
					.setRefrigeratorTargetTemperature(parseTemperature(value));
			break;
		case MieleApplianceConstants.REFRIGERATOR_CURRENT_TEMPERATURE:
			// Downcast using constructor
			if (!(this.tempApplianceMessage instanceof MieleFridgeFreezerInfoMessage)) {
				this.tempApplianceMessage = new MieleFridgeFreezerInfoMessage(this.tempApplianceMessage);
			}
			((MieleFridgeFreezerInfoMessage) this.tempApplianceMessage).setRefrigeratorTemperature(parseTemperature(value));
			break;
		case MieleApplianceConstants.FREEZER_STATE:
			// Downcast using constructor
			if (!(this.tempApplianceMessage instanceof MieleFridgeFreezerInfoMessage)) {
				this.tempApplianceMessage = new MieleFridgeFreezerInfoMessage(this.tempApplianceMessage);
			}
			((MieleFridgeFreezerInfoMessage) this.tempApplianceMessage).setFreezerState(MieleApplianceUtil.getState(
					this.language, value));
			break;
		case MieleApplianceConstants.FREEZER_TARGET_TEMPERATURE:
			// Downcast using constructor
			if (!(this.tempApplianceMessage instanceof MieleFridgeFreezerInfoMessage)) {
				this.tempApplianceMessage = new MieleFridgeFreezerInfoMessage(this.tempApplianceMessage);
			}
			((MieleFridgeFreezerInfoMessage) this.tempApplianceMessage).setFreezerTargetTemperature(parseTemperature(value));
			break;
		case MieleApplianceConstants.FREEZER_CURRENT_TEMPERATURE:
			// Downcast using constructor
			if (!(this.tempApplianceMessage instanceof MieleFridgeFreezerInfoMessage)) {
				this.tempApplianceMessage = new MieleFridgeFreezerInfoMessage(this.tempApplianceMessage);
			}
			((MieleFridgeFreezerInfoMessage) this.tempApplianceMessage).setFreezerTemperature(parseTemperature(value));
			break;
		case MieleApplianceConstants.DISHWASHER_START_TIME:
			// Downcast using constructor
			if (!(this.tempApplianceMessage instanceof MieleDishWasherInfoMessage)) {
				this.tempApplianceMessage = new MieleDishWasherInfoMessage(this.tempApplianceMessage);
			}
			((MieleDishWasherInfoMessage) this.tempApplianceMessage).setStartTime(parseTimeToDate(value));
			break;
		case MieleApplianceConstants.DISHWASHER_REMAINING_TIME:
			// Downcast using constructor
			if (!(this.tempApplianceMessage instanceof MieleDishWasherInfoMessage)) {
				this.tempApplianceMessage = new MieleDishWasherInfoMessage(this.tempApplianceMessage);
			}
			((MieleDishWasherInfoMessage) this.tempApplianceMessage).setRemainingTime(parseTimeToMinutes(value));
			break;
		case MieleApplianceConstants.DISHWASHER_DURATION:
			// Downcast using constructor
			if (!(this.tempApplianceMessage instanceof MieleDishWasherInfoMessage)) {
				this.tempApplianceMessage = new MieleDishWasherInfoMessage(this.tempApplianceMessage);
			}
			((MieleDishWasherInfoMessage) this.tempApplianceMessage).setDuration(parseTimeToMinutes(value));
			break;
		case MieleApplianceConstants.DISHWASHER_PROGRAM:
			// Downcast using constructor
			if (!(this.tempApplianceMessage instanceof MieleDishWasherInfoMessage)) {
				this.tempApplianceMessage = new MieleDishWasherInfoMessage(this.tempApplianceMessage);
			}
			((MieleDishWasherInfoMessage) this.tempApplianceMessage).setProgram(value);
			break;
		case MieleApplianceConstants.DISHWASHER_PHASE:
			// Downcast using constructor
			if (!(this.tempApplianceMessage instanceof MieleDishWasherInfoMessage)) {
				this.tempApplianceMessage = new MieleDishWasherInfoMessage(this.tempApplianceMessage);
			}
			((MieleDishWasherInfoMessage) this.tempApplianceMessage).setPhase(value);
			break;
		default:
			// Do nothing
			break;
		}

	}

	/**
	 * Parse and convert a temperature string into a float.
	 * 
	 * @param s
	 *            The temperature string.
	 * @return The temperature converted to float.
	 * @throws SAXException
	 */
	private Float parseTemperature(final String s) throws SAXException {
		Float temperature = null;

		if (s != null && !s.trim().isEmpty()) {
			try {
				String curTempStr = s.replace("°C", "");
				temperature = Float.parseFloat(curTempStr);

			} catch (NumberFormatException nfe) {
				String msg = "Parsing error temperature value. Could not convert: " + s;
				throw new SAXException(msg);
			}
		} else {
			logger.warn("Could not parse temperature. Parameter string: " + s);
		}
		return temperature;
	}

	/**
	 * Parse and convert date string to a Date object.
	 * 
	 * @param s
	 *            The string representing a Date.
	 * @return The date object created from the input string.
	 * @throws SAXException
	 */
	private Date parseTimeToDate(final String s) throws SAXException {
		int timeInMinutes = parseTimeToMinutes(s);

		// Get current date-time (note that string only contains a
		// time and not a date)
		Calendar current = Calendar.getInstance();

		// Clone the current time and set the time from the time string
		Calendar cal = (Calendar) current.clone();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.add(Calendar.MINUTE, timeInMinutes);

		// Is current time already after start time then set it to tomorrow
		if (current.after(cal)) {
			cal.add(Calendar.DATE, 1);
		}

		// Return the start time
		return cal.getTime();
	}

	/**
	 * Parse time string converted to minutes passed since midnight (00:00 H).
	 * The expected format of the time string is hh:mmH (for example 05:10 H) or
	 * just a number representing a number of minutes.
	 * 
	 * @param s
	 *            The time string.
	 * @return The converted time in minutes passed since midnight.
	 * @throws SAXException
	 */
	private int parseTimeToMinutes(final String s) throws SAXException {
		int hours = 0;
		int minutes = 0;

		// Throw error when null
		if (s == null) {
			throw new SAXException("Could not parse time string. Parameter string is null.");
		} else if (s.trim().isEmpty()) {
			throw new SAXException("Could not parse time string. Parameter string is empty.");
		}

		// Get the minute and hour substrings
		String[] timeString = (s.toUpperCase().replace('H', ' ')).split(":");
		if (timeString.length > 2 && timeString.length >= 1) {
			throw new SAXException("Parsing error time string. Wrong format '" + s + "'");
		}

		try {
			if (timeString.length == 1) {
				// Time is only in minute format
				minutes = Integer.parseInt(timeString[0].trim());
			} else {
				// Hours and minutes in format
				hours = Integer.parseInt(timeString[0].trim());
				minutes = Integer.parseInt(timeString[1].trim());
			}
		} catch (NumberFormatException nfe) {
			String msg = "Parsing error time string. Could not parse '" + s + "'";
			throw new SAXException(msg);
		}

		return ((hours * 60) + minutes);
	}

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes)
			throws SAXException {
		this.tempVal = "";

		if (qName.equalsIgnoreCase(MieleGatewayXmlConstants.ELEMENT_DEVICES)) {
			// Start tag for appliance list message
			this.message = new MieleApplianceListMessage();

		} else if (qName.equalsIgnoreCase(MieleGatewayXmlConstants.ELEMENT_DEVICE)) {
			// Device can be part of the appliance info or the appliance list
			// message
			if (this.message == null) {
				// Start tag for appliance info message
				this.message = new MieleApplianceInfoMessage();
			}
			this.tempApplianceMessage = new MieleApplianceInfoMessage();
		} else if (qName.equalsIgnoreCase(MieleGatewayXmlConstants.ELEMENT_OK)) {
			// Action OK response message received
			this.message = new MieleGatewayActionOkMessage();
		} else if (qName.equalsIgnoreCase(MieleGatewayXmlConstants.ELEMENT_ERROR)) {
			// Error response message received
			this.message = new MieleGatewayErrorMessage();
		} else if (qName.equalsIgnoreCase(MieleGatewayXmlConstants.ELEMENT_INFORMATION)) {
			// Information 'key-value' pair elements will be stored in the
			// temporary map
			// tempInformationMap = new HashMap<String, String>();
		} else if (qName.equalsIgnoreCase(MieleGatewayXmlConstants.ELEMENT_KEY)) {
			// Add the new information key-value pair to the temporary map
			String name = attributes.getValue(MieleGatewayXmlConstants.ATTR_KEY_NAME);
			String value = attributes.getValue(MieleGatewayXmlConstants.ATTR_KEY_VALUE);
			parseKeyValue(name, value);
		} else if (qName.equalsIgnoreCase(MieleGatewayXmlConstants.ELEMENT_ACTIONS)) {
			// Action 'action name-action url' elements will be stored in the
			// temporary map
			this.tempActionMap = new HashMap<String, String>();
		} else if (qName.equalsIgnoreCase(MieleGatewayXmlConstants.ELEMENT_ACTION)) {
			if (!(this.message instanceof MieleGatewayActionOkMessage || this.message instanceof MieleGatewayErrorMessage)) {
				String name = attributes.getValue(MieleGatewayXmlConstants.ATTR_ACTION_NAME);
				String url = attributes.getValue(MieleGatewayXmlConstants.ATTR_ACTION_URL);

				this.tempActionMap.put(name, url);
			}
		}

	}
}
