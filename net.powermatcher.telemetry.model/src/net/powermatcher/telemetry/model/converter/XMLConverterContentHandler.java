package net.powermatcher.telemetry.model.converter;


import java.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import net.powermatcher.telemetry.model.constants.TelemetryModelConstants;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * @author IBM
 * @version 0.9.0
 */
public class XMLConverterContentHandler extends DefaultHandler implements TelemetryModelConstants {
	/**
	 * @author IBM
	 * @version 0.9.0
	 */
	private class AlertState extends XMLConverterContentHandler.State {
		/**
		 * Constructs an instance of this class from the specified parent state
		 * and map parameters.
		 * 
		 * @param parentState
		 *            The parent state (<code>State</code>) parameter.
		 * @param map
		 *            The map (<code>Map<String,Object></code>) parameter.
		 */
		public AlertState(final XMLConverterContentHandler.State parentState, final Map<String, Object> map) {
			super(parentState, map);
		}

		/**
		 * Gets the name (String) value.
		 * 
		 * @return The name (<code>String</code>) value.
		 */
		@Override
		protected String getName() {
			return ELEM_ALERT;
		}

	}

	/**
	 * @author IBM
	 * @version 0.9.0
	 */
	private class ControlState extends XMLConverterContentHandler.State {
		/**
		 * Constructs an instance of this class from the specified parent state
		 * and map parameters.
		 * 
		 * @param parentState
		 *            The parent state (<code>State</code>) parameter.
		 * @param map
		 *            The map (<code>Map<String,Object></code>) parameter.
		 */
		public ControlState(final XMLConverterContentHandler.State parentState, final Map<String, Object> map) {
			super(parentState, map);
		}

		/**
		 * Gets the name (String) value.
		 * 
		 * @return The name (<code>String</code>) value.
		 */
		@Override
		protected String getName() {
			return ELEM_CONTROL;
		}

	}

	/**
	 * Represents processing of the elements that contain the body of a telemetry
	 * message
	 * 
	 * @author IBM
	 * @version 0.9.0
	 */
	private class TelemetryState extends XMLConverterContentHandler.State {
		/**
		 * Constructs an instance of this class from the specified parent state
		 * and map parameters.
		 * 
		 * @param parentState
		 *            The parent state (<code>State</code>) parameter.
		 * @param map
		 *            The map (<code>Map<String,Object></code>) parameter.
		 * @param localName 
		 *            The local name (<code>String</code>) parameter.
		 */
		public TelemetryState(final XMLConverterContentHandler.State parentState, final Map<String, Object> map, String localName) {
			super(parentState, map, localName);
		}

		/**
		 * Gets the name (String) value.
		 * 
		 * @return The name (<code>String</code>) value.
		 */
		@Override
		protected String getName() {
			return ELEM_TELEMETRY;
		}

		/**
		 * Start element with the specified local name and atts parameters.
		 * 
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @param atts
		 *            The atts (<code>Attributes</code>) parameter.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 */
		@Override
		protected void startElement(final String localName, final Attributes atts) throws SAXParseException {
			if (ELEM_MEASUREMENT.equals(localName)) {
				String valueName = getStringAttribute(atts, localName, ATTR_VALUE_NAME, true);
				String units = getStringAttribute(atts, localName, ATTR_UNITS, true);
				addNestedMap(MEASUREMENT_DATA_KEY);
				addNestedDataKey(VALUE_NAME_DATA_KEY, valueName);
				addNestedDataKey(UNITS_DATA_KEY, units);
				setState(new XMLConverterContentHandler.MeasurementState(this, getNestedMap()));
			} else if (ELEM_STATUS.equals(localName)) {
				String valueName = getStringAttribute(atts, localName, ATTR_VALUE_NAME, true);
				addNestedMap(STATUS_DATA_KEY);
				addNestedDataKey(VALUE_NAME_DATA_KEY, valueName);
				setState(new XMLConverterContentHandler.StatusState(this, getNestedMap()));
			} else if (ELEM_ALERT.equals(localName)) {
				String value = getStringAttribute(atts, localName, ATTR_VALUE, true);
				Date timestamp = getDateTimeAttribute(atts, localName, ATTR_TIMESTAMP, true);
				addNestedMap(ALERT_DATA_KEY);
				addNestedDataKey(VALUE_DATA_KEY, value);
				addNestedDataKey(TIMESTAMP_DATA_KEY, timestamp);
				setState(new XMLConverterContentHandler.AlertState(this, getNestedMap()));
			} else if (ELEM_CONTROL.equals(localName)) {
				String valueName = getStringAttribute(atts, localName, ATTR_VALUE_NAME, true);
				String value = getStringAttribute(atts, localName, ATTR_VALUE, true);
				String units = getStringAttribute(atts, localName, ATTR_UNITS, true);
				Date timestamp = getDateTimeAttribute(atts, localName, ATTR_TIMESTAMP, true);
				addNestedMap(CONTROL_DATA_KEY);
				addNestedDataKey(VALUE_NAME_DATA_KEY, valueName);
				addNestedDataKey(VALUE_DATA_KEY, value);
				addNestedDataKey(UNITS_DATA_KEY, units);
				addNestedDataKey(TIMESTAMP_DATA_KEY, timestamp);
				setState(new XMLConverterContentHandler.ControlState(this, getNestedMap()));
			} else if (ELEM_REQUEST.equals(localName)) {
				String requestType = getStringAttribute(atts, localName, ATTR_REQUEST_TYPE, true);
				String requestId = getStringAttribute(atts, localName, ATTR_REQUEST_ID, false);
				Date timestamp = getDateTimeAttribute(atts, localName, ATTR_TIMESTAMP, true);
				addNestedMap(REQUEST_DATA_KEY);
				addNestedDataKey(REQUEST_TYPE_DATA_KEY, requestType);
				addNestedDataKey(REQUEST_ID_DATA_KEY, requestId);
				addNestedDataKey(TIMESTAMP_DATA_KEY, timestamp);
				setState(new XMLConverterContentHandler.RequestState(this, getNestedMap()));
			} else if (ELEM_RESPONSE.equals(localName)) {
				String requestType = getStringAttribute(atts, localName, ATTR_REQUEST_TYPE, true);
				String requestId = getStringAttribute(atts, localName, ATTR_REQUEST_ID, false);
				Date timestamp = getDateTimeAttribute(atts, localName, ATTR_TIMESTAMP, true);
				addNestedMap(RESPONSE_DATA_KEY);
				addNestedDataKey(REQUEST_TYPE_DATA_KEY, requestType);
				addNestedDataKey(REQUEST_ID_DATA_KEY, requestId);
				addNestedDataKey(TIMESTAMP_DATA_KEY, timestamp);
				setState(new XMLConverterContentHandler.ResponseState(this, getNestedMap()));
			} else {
				unexpectedStartElement(localName);
			}
		}

	}

	/**
	 * @author IBM
	 * @version 0.9.0
	 */
	class EndDocumentState extends XMLConverterContentHandler.State {
		/**
		 * Constructs an instance of this class.
		 */
		public EndDocumentState() {
			super(null, null);
		}

		/**
		 * Gets the name (String) value.
		 * 
		 * @return The name (<code>String</code>) value.
		 */
		@Override
		protected String getName() {
			return null;
		}

	}

	/**
	 * @author IBM
	 * @version 0.9.0
	 */
	private class MeasurementState extends XMLConverterContentHandler.State {
		/**
		 * Constructs an instance of this class from the specified parent state
		 * and map parameters.
		 * 
		 * @param parentState
		 *            The parent state (<code>State</code>) parameter.
		 * @param map
		 *            The map (<code>Map<String,Object></code>) parameter.
		 */
		public MeasurementState(final XMLConverterContentHandler.State parentState, final Map<String, Object> map) {
			super(parentState, map);
		}

		/**
		 * Gets the name (String) value.
		 * 
		 * @return The name (<code>String</code>) value.
		 */
		@Override
		protected String getName() {
			return ELEM_MEASUREMENT;
		}

		/**
		 * Start element with the specified local name and atts parameters.
		 * 
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @param atts
		 *            The atts (<code>Attributes</code>) parameter.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 */
		@Override
		protected void startElement(final String localName, final Attributes atts) throws SAXParseException {
			if (getName().equals(getCurrentElement())) {
				if (ELEM_SINGLE_VALUE.equals(localName)) {
					Float value = getFloatAttribute(atts, localName, ATTR_VALUE, true);
					Date timestamp = getDateTimeAttribute(atts, localName, ATTR_TIMESTAMP, true);
					Integer period = getIntAttribute(atts, localName, ATTR_PERIOD, false);
					addNestedMap(SINGLE_VALUE_DATA_KEY);
					addNestedDataKey(VALUE_DATA_KEY, value);
					addNestedDataKey(TIMESTAMP_DATA_KEY, timestamp);
					addNestedDataKey(PERIOD_DATA_KEY, period);
					setCurrentElement(localName);
				} else {
					unexpectedStartElement(localName);
				}
			} else {
				unexpectedStartElement(localName);
			}
		}

	}

	/**
	 * @author IBM
	 * @version 0.9.0
	 */
	private class RequestState extends XMLConverterContentHandler.State {
		/**
		 * Constructs an instance of this class from the specified parent state
		 * and map parameters.
		 * 
		 * @param parentState
		 *            The parent state (<code>State</code>) parameter.
		 * @param map
		 *            The map (<code>Map<String,Object></code>) parameter.
		 */
		public RequestState(final XMLConverterContentHandler.State parentState, final Map<String, Object> map) {
			super(parentState, map);
		}

		/**
		 * Gets the name (String) value.
		 * 
		 * @return The name (<code>String</code>) value.
		 */
		@Override
		protected String getName() {
			return ELEM_REQUEST;
		}

		/**
		 * Start element with the specified local name and atts parameters.
		 * 
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @param atts
		 *            The atts (<code>Attributes</code>) parameter.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 */
		@Override
		protected void startElement(final String localName, final Attributes atts) throws SAXParseException {
			if (getName().equals(getCurrentElement())) {
				if (ELEM_PROPERTY.equals(localName)) {
					String name = getStringAttribute(atts, localName, ATTR_NAME, true);
					String value = getStringAttribute(atts, localName, ATTR_VALUE, true);
					Boolean logged = getBooleanAttribute(atts, localName, ATTR_LOGGING, false);
					addNestedMap(PROPERTY_DATA_KEY);
					addNestedDataKey(NAME_DATA_KEY, name);
					addNestedDataKey(VALUE_DATA_KEY, value);
					addNestedDataKey(LOGGING_DATA_KEY, logged);
					setCurrentElement(localName);
				} else {
					unexpectedStartElement(localName);
				}
			} else {
				unexpectedStartElement(localName);
			}
		}

	}

	/**
	 * @author IBM
	 * @version 0.9.0
	 */
	private class ResponseState extends XMLConverterContentHandler.State {
		/**
		 * Constructs an instance of this class from the specified parent state
		 * and map parameters.
		 * 
		 * @param parentState
		 *            The parent state (<code>State</code>) parameter.
		 * @param map
		 *            The map (<code>Map<String,Object></code>) parameter.
		 */
		public ResponseState(final XMLConverterContentHandler.State parentState, final Map<String, Object> map) {
			super(parentState, map);
		}

		/**
		 * Gets the name (String) value.
		 * 
		 * @return The name (<code>String</code>) value.
		 */
		@Override
		protected String getName() {
			return ELEM_RESPONSE;
		}

		/**
		 * Start element with the specified local name and atts parameters.
		 * 
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @param atts
		 *            The atts (<code>Attributes</code>) parameter.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 */
		@Override
		protected void startElement(final String localName, final Attributes atts) throws SAXParseException {
			if (getName().equals(getCurrentElement())) {
				if (ELEM_PROPERTY.equals(localName)) {
					String name = getStringAttribute(atts, localName, ATTR_NAME, true);
					String value = getStringAttribute(atts, localName, ATTR_VALUE, true);
					Boolean logged = getBooleanAttribute(atts, localName, ATTR_LOGGING, false);
					addNestedMap(PROPERTY_DATA_KEY);
					addNestedDataKey(NAME_DATA_KEY, name);
					addNestedDataKey(VALUE_DATA_KEY, value);
					addNestedDataKey(LOGGING_DATA_KEY, logged);
					setCurrentElement(localName);
				} else {
					unexpectedStartElement(localName);
				}
			} else {
				unexpectedStartElement(localName);
			}
		}

	}

	/**
	 * @author IBM
	 * @version 0.9.0
	 */
	private class StartDocumentState extends XMLConverterContentHandler.State {
		/**
		 * Constructs an instance of this class.
		 */
		public StartDocumentState() {
			super(null, null);
		}

		/**
		 * Gets the name (String) value.
		 * 
		 * @return The name (<code>String</code>) value.
		 */
		@Override
		protected String getName() {
			return null;
		}

		/**
		 * Start element with the specified local name and atts parameters.
		 * 
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @param atts
		 *            The atts (<code>Attributes</code>) parameter.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 */
		@Override
		protected void startElement(final String localName, final Attributes atts) throws SAXParseException {
			if (ELEM_TELEMETRY.equals(localName) || LEGACY_ELEM_TELEMETRY.equals(localName)) {
				String namespaceId = getStringAttribute(atts, localName, ATTR_CLUSTER_ID, false);
				if (namespaceId == null) {
					namespaceId = getStringAttribute(atts, localName, LEGACY_ATTR_CLUSTER_ID, false);
				}
				String agentId = getStringAttribute(atts, localName, ATTR_AGENT_ID, false);
				if (agentId == null) {
					agentId = getStringAttribute(atts, localName, LEGACY_ATTR_AGENT_ID, true);
				}
				Map<String, Object> map = getMap();
				map.put(CLUSTER_ID_DATA_KEY, namespaceId);
				map.put(AGENT_ID_DATA_KEY, agentId);
				setState(new XMLConverterContentHandler.TelemetryState(this, map, localName));
			} else {
				unexpectedStartElement(localName);
			}
		}

	}

	/**
	 * @author IBM
	 * @version 0.9.0
	 */
	private abstract class State {
		/**
		 * Define the parent state (State) field.
		 */
		private State parentState;
		/**
		 * Define the current element (String) field.
		 */
		private String currentElement;
		/**
		 * Define the map (Map) field.
		 */
		private Map<String, Object> map;
		/**
		 * Define the nested map (Map) field.
		 */
		private Map<String, Object> nestedMap;
		/**
		 * Define the index (int) field.
		 */
		private int index = 0;

		/**
		 * Constructs an instance of this class from the specified parent state
		 * parameter.
		 * 
		 * @param parentState
		 *            The parent state (<code>State</code>) parameter.
		 * @param map
		 *            The map (<code>Map<String,Object></code>) parameter.
		 */
		protected State(final XMLConverterContentHandler.State parentState, final Map<String, Object> map) {
			this.parentState = parentState;
			this.map = map;
			setCurrentElement(getName());
		}

		/**
		 * Constructs an instance of this class from the specified parent state
		 * parameter.
		 * 
		 * @param parentState
		 *            The parent state (<code>State</code>) parameter.
		 * @param map
		 *            The map (<code>Map<String,Object></code>) parameter.
		 * @param localName 
		 *            The local name (<code>String</code>) parameter.
		 */
		protected State(final XMLConverterContentHandler.State parentState, final Map<String, Object> map, String localName) {
			this.parentState = parentState;
			this.map = map;
			setCurrentElement(localName);
		}

		/**
		 * Add data key with the specified key and value parameters.
		 * 
		 * @param key
		 *            The key (<code>String</code>) parameter.
		 * @param value
		 *            The value (<code>Object</code>) parameter.
		 * @see #addNestedDataKey(String,Object)
		 */
		public void addDataKey(final String key, final Object value) {
			if (value != null) {
				this.map.put(key, value);
			}
		}

		/**
		 * Add nested data key with the specified key and value parameters.
		 * 
		 * @param key
		 *            The key (<code>String</code>) parameter.
		 * @param value
		 *            The value (<code>Object</code>) parameter.
		 */
		public void addNestedDataKey(final String key, final Object value) {
			if (value != null) {
				this.nestedMap.put(key, value);
			}
		}

		/**
		 * Add nested map with the specified data key parameter.
		 * 
		 * @param dataKey
		 *            The data key (<code>String</code>) parameter.
		 * @see #getNestedMap()
		 */
		public void addNestedMap(final String dataKey) {
			this.nestedMap = new HashMap<String, Object>();
			addDataKey(dataKey + getNextIndex(), this.nestedMap);
		}

		/**
		 * Characters with the specified ch, start and length parameters.
		 * 
		 * @param ch
		 *            The ch (<code>char[]</code>) parameter.
		 * @param start
		 *            The start (<code>int</code>) parameter.
		 * @param length
		 *            The length (<code>int</code>) parameter.
		 */
		public void characters(final char[] ch, final int start, final int length) {
			/* do nothing */
		}

		/**
		 * End document.
		 * 
		 * @see #startDocument()
		 */
		protected void endDocument() {
			setState(new XMLConverterContentHandler.EndDocumentState());
		}

		/**
		 * End element with the specified local name parameter.
		 * 
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 * @see #getCurrentElement()
		 * @see #setCurrentElement(String)
		 * @see #startElement(String,Attributes)
		 * @see #unexpectedEndElement(String)
		 * @see #unexpectedStartElement(String)
		 */
		protected void endElement(final String localName) throws SAXParseException {
			if (localName.equals(getCurrentElement())) {
				if (localName.equals(getName())) {
					returnToParent();
				} else {
					setCurrentElement(getName());
				}
			} else {
				unexpectedEndElement(localName);
			}
		}

		/**
		 * Get boolean attribute with the specified atts, local name, attr and
		 * required parameters and return the Boolean result.
		 * 
		 * @param atts
		 *            The atts (<code>Attributes</code>) parameter.
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @param attr
		 *            The attr (<code>String</code>) parameter.
		 * @param required
		 *            The required (<code>boolean</code>) parameter.
		 * @return Results of the get boolean attribute (<code>Boolean</code>)
		 *         value.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 */
		protected Boolean getBooleanAttribute(final Attributes atts, final String localName, final String attr,
				final boolean required) throws SAXParseException {
			String value = getStringAttribute(atts, localName, attr, required);
			if (value != null) {
				try {
					return new Boolean(value);
				} catch (final NumberFormatException e) {
					unexpectedAttributeValue(localName, attr, value, e.getMessage());
				}
			}
			return null;
		}

		/**
		 * Gets the current element (String) value.
		 * 
		 * @return The current element (<code>String</code>) value.
		 * @see #setCurrentElement(String)
		 */
		public String getCurrentElement() {
			return this.currentElement;
		}

		/**
		 * Get date time attribute with the specified atts, local name, attr and
		 * required parameters and return the Date result.
		 * 
		 * @param atts
		 *            The atts (<code>Attributes</code>) parameter.
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @param attr
		 *            The attr (<code>String</code>) parameter.
		 * @param required
		 *            The required (<code>boolean</code>) parameter.
		 * @return Results of the get date time attribute (<code>Date</code>)
		 *         value.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 */
		protected Date getDateTimeAttribute(final Attributes atts, final String localName, final String attr,
				final boolean required) throws SAXParseException {
			String value = getStringAttribute(atts, localName, attr, required);
			if (value != null) {
				try {
					return parseISO8601Date(value);
				} catch (final NumberFormatException e) {
					unexpectedAttributeValue(localName, attr, value, e.getMessage());
				}
			}
			return null;
		}

		/**
		 * Get float attribute with the specified atts, local name, attr and
		 * required parameters and return the Float result.
		 * 
		 * @param atts
		 *            The atts (<code>Attributes</code>) parameter.
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @param attr
		 *            The attr (<code>String</code>) parameter.
		 * @param required
		 *            The required (<code>boolean</code>) parameter.
		 * @return Results of the get float attribute (<code>Float</code>)
		 *         value.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 */
		protected Float getFloatAttribute(final Attributes atts, final String localName, final String attr,
				final boolean required) throws SAXParseException {
			String value = getStringAttribute(atts, localName, attr, required);
			if (value != null) {
				try {
					return new Float(value);
				} catch (final NumberFormatException e) {
					unexpectedAttributeValue(localName, attr, value, e.getMessage());
				}
			}
			return null;
		}

		/**
		 * Get int attribute with the specified atts, local name, attr and
		 * required parameters and return the Integer result.
		 * 
		 * @param atts
		 *            The atts (<code>Attributes</code>) parameter.
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @param attr
		 *            The attr (<code>String</code>) parameter.
		 * @param required
		 *            The required (<code>boolean</code>) parameter.
		 * @return Results of the get int attribute (<code>Integer</code>)
		 *         value.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 */
		protected Integer getIntAttribute(final Attributes atts, final String localName, final String attr,
				final boolean required) throws SAXParseException {
			String value = getStringAttribute(atts, localName, attr, required);
			if (value != null) {
				try {
					return new Integer(value);
				} catch (final NumberFormatException e) {
					unexpectedAttributeValue(localName, attr, value, e.getMessage());
				}
			}
			return null;
		}

		/**
		 * Gets the name (String) value.
		 * 
		 * @return The name (<code>String</code>) value.
		 */
		protected abstract String getName();

		/**
		 * Gets the nested map (Map<String,Object>) value.
		 * 
		 * @return The nested map (<code>Map</code>) value.
		 * @see #addNestedMap(String)
		 */
		public Map<String, Object> getNestedMap() {
			return this.nestedMap;
		}

		/**
		 * Gets the next index (String) value.
		 * 
		 * @return The next index (<code>String</code>) value.
		 */
		private String getNextIndex() {
			return "_" + Integer.toString(this.index++);
		}

		/**
		 * Get string attribute with the specified atts, local name, attr and
		 * required parameters and return the String result.
		 * 
		 * @param atts
		 *            The atts (<code>Attributes</code>) parameter.
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @param attr
		 *            The attr (<code>String</code>) parameter.
		 * @param required
		 *            The required (<code>boolean</code>) parameter.
		 * @return Results of the get string attribute (<code>String</code>)
		 *         value.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 */
		protected String getStringAttribute(final Attributes atts, final String localName, final String attr,
				final boolean required) throws SAXParseException {
			String value = atts.getValue(attr);
			if (required && value == null) {
				missingAttribute(localName, attr);
			}
			return value;
		}

		/**
		 * Missing attribute with the specified local name and attr parameters.
		 * 
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @param attr
		 *            The attr (<code>String</code>) parameter.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 * @see #getBooleanAttribute(Attributes,String,String,boolean)
		 * @see #getDateTimeAttribute(Attributes,String,String,boolean)
		 * @see #getFloatAttribute(Attributes,String,String,boolean)
		 * @see #getIntAttribute(Attributes,String,String,boolean)
		 * @see #getStringAttribute(Attributes,String,String,boolean)
		 */
		protected void missingAttribute(final String localName, final String attr) throws SAXParseException {
			final Object[] parms = { localName, attr, new Integer(getLineNumber()) };
			throw new SAXParseException(MessageFormat.format("Missing XML attribute {1} for <{0}> at line {2}", parms), null,
					null, getLineNumber(), 0);
		}

		/**
		 * Return to parent.
		 */
		protected void returnToParent() {
			setState(this.parentState);
		}

		/**
		 * Sets the current element value.
		 * 
		 * @param currentElement
		 *            The current element (<code>String</code>) parameter.
		 * @see #getCurrentElement()
		 */
		public void setCurrentElement(final String currentElement) {
			this.currentElement = currentElement;
		}

		/**
		 * Start document.
		 * 
		 * @see #endDocument()
		 */
		protected void startDocument() {
			setState(new XMLConverterContentHandler.StartDocumentState());
		}

		/**
		 * Start element with the specified local name and atts parameters.
		 * 
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @param atts
		 *            The atts (<code>Attributes</code>) parameter.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 * @see #endElement(String)
		 * @see #getCurrentElement()
		 * @see #setCurrentElement(String)
		 * @see #unexpectedEndElement(String)
		 * @see #unexpectedStartElement(String)
		 */
		protected void startElement(final String localName, final Attributes atts) throws SAXParseException {
			unexpectedStartElement(localName);
		}

		/**
		 * Unexpected attribute value with the specified attr, value and message
		 * parameters.
		 * 
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @param attr
		 *            The attr (<code>String</code>) parameter.
		 * @param value
		 *            The value (<code>String</code>) parameter.
		 * @param message
		 *            The message (<code>String</code>) parameter.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 */
		protected void unexpectedAttributeValue(final String localName, final String attr, final String value,
				final String message) throws SAXParseException {
			final Object[] parms = { localName, attr, value, message, new Integer(getLineNumber()) };
			throw new SAXParseException(MessageFormat.format("Invalid XML attribute {1}=\"{2}\" for <{0}> at line {4}; {3}",
					parms), null, null, getLineNumber(), 0);
		}

		/**
		 * Unexpected end element with the specified local name parameter.
		 * 
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 */
		protected void unexpectedEndElement(final String localName) throws SAXParseException {
			final Object[] parms = { localName, new Integer(getLineNumber()) };
			throw new SAXParseException(MessageFormat.format("Unexpected XML end element <{0}> at line {1}", parms), null,
					null, getLineNumber(), 0);
		}

		/**
		 * Unexpected start element with the specified local name parameter.
		 * 
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 */
		protected void unexpectedStartElement(final String localName) throws SAXParseException {
			final Object[] parms = { localName, new Integer(getLineNumber()) };
			throw new SAXParseException(MessageFormat.format("Unexpected XML start element <{0}> at line {1}", parms), null,
					null, getLineNumber(), 0);
		}

	}

	/**
	 * @author IBM
	 * @version 0.9.0
	 */
	private class StatusState extends XMLConverterContentHandler.State {
		/**
		 * Constructs an instance of this class from the specified parent state
		 * and map parameters.
		 * 
		 * @param parentState
		 *            The parent state (<code>State</code>) parameter.
		 * @param map
		 *            The map (<code>Map<String,Object></code>) parameter.
		 */
		public StatusState(final XMLConverterContentHandler.State parentState, final Map<String, Object> map) {
			super(parentState, map);
		}

		/**
		 * Gets the name (String) value.
		 * 
		 * @return The name (<code>String</code>) value.
		 */
		@Override
		protected String getName() {
			return ELEM_STATUS;
		}

		/**
		 * Start element with the specified local name and atts parameters.
		 * 
		 * @param localName
		 *            The local name (<code>String</code>) parameter.
		 * @param atts
		 *            The atts (<code>Attributes</code>) parameter.
		 * @throws SAXParseException
		 *             SAXParse Exception.
		 */
		@Override
		protected void startElement(final String localName, final Attributes atts) throws SAXParseException {
			if (getName().equals(getCurrentElement())) {
				if (ELEM_SINGLE_VALUE.equals(localName)) {
					String value = getStringAttribute(atts, localName, ATTR_VALUE, true);
					Date timestamp = getDateTimeAttribute(atts, localName, ATTR_TIMESTAMP, true);
					addNestedMap(SINGLE_VALUE_DATA_KEY);
					addNestedDataKey(VALUE_DATA_KEY, value);
					addNestedDataKey(TIMESTAMP_DATA_KEY, timestamp);
					setCurrentElement(localName);
				} else {
					unexpectedStartElement(localName);
				}
			} else {
				unexpectedStartElement(localName);
			}
		}

	}

	/**
	 * Define the state (State) field.
	 */
	private State state;
	/**
	 * Define the lineNumber (int) field.
	 */
	private int lineNumber;
	/**
	 * Define the map (Map) field.
	 */
	private Map<String, Object> map;

	/**
	 * Constructs an instance of this class.
	 */
	public XMLConverterContentHandler() {
		super();
		this.lineNumber = 1;
		this.map = new HashMap<String, Object>();
		setState(new XMLConverterContentHandler.EndDocumentState());
	}

	/**
	 * Characters with the specified ch, start and length parameters.
	 * 
	 * @param ch
	 *            The ch (<code>char[]</code>) parameter.
	 * @param start
	 *            The start (<code>int</code>) parameter.
	 * @param length
	 *            The length (<code>int</code>) parameter.
	 * @throws SAXException
	 *             SAXException.
	 */
	@Override
	public void characters(final char[] ch, final int start, final int length) throws SAXException {
		this.state.characters(ch, start, length);
		countLines(ch, start, length);
	}

	/**
	 * Count lines with the specified ch, start and length parameters.
	 * 
	 * @param ch
	 *            The ch (<code>char[]</code>) parameter.
	 * @param start
	 *            The start (<code>int</code>) parameter.
	 * @param length
	 *            The length (<code>int</code>) parameter.
	 */
	private void countLines(final char[] ch, final int start, final int length) {
		int i = start;
		final int end = start + length;
		while (i < end) {
			if (ch[i++] == '\n') {
				this.lineNumber += 1;
			}
		}
	}

	/**
	 * End document.
	 */
	@Override
	public void endDocument() {
		this.state.endDocument();
	}

	/**
	 * End element with the specified namespace uri, local name and q name
	 * parameters.
	 * 
	 * @param namespaceURI
	 *            The namespace uri (<code>String</code>) parameter.
	 * @param localName
	 *            The local name (<code>String</code>) parameter.
	 * @param qName
	 *            The q name (<code>String</code>) parameter.
	 * @throws SAXException
	 *             SAXException.
	 * @see #startElement(String, String, String, Attributes)
	 */
	@Override
	public void endElement(final String namespaceURI, final String localName, final String qName) throws SAXException {
		this.state.endElement(localName);
	}

	/**
	 * Gets the line number (int) value.
	 * 
	 * @return The line number (<code>int</code>) value.
	 */
	public int getLineNumber() {
		return this.lineNumber;
	}

	/**
	 * Gets the map (Map<String,Object>) value.
	 * 
	 * @return The map (<code>Map</code>) value.
	 */
	public Map<String, Object> getMap() {
		return this.map;
	}

	/**
	 * Ignorable whitespace with the specified ch, start and length parameters.
	 * 
	 * @param ch
	 *            The ch (<code>char[]</code>) parameter.
	 * @param start
	 *            The start (<code>int</code>) parameter.
	 * @param length
	 *            The length (<code>int</code>) parameter.
	 * @throws SAXException
	 *             SAXException.
	 */
	@Override
	public void ignorableWhitespace(final char[] ch, final int start, final int length) throws SAXException {
		countLines(ch, start, length);
	}

	/**
	 * Parse iso8601 date with the specified value parameter and return the Date
	 * result.
	 * 
	 * @param value
	 *            The value (<code>String</code>) parameter.
	 * @return Results of the parse iso8601 date (<code>Date</code>) value.
	 */
	private Date parseISO8601Date(final String value) {
		// yyyy-MM-ddTHH:mm:ss.MMMZ
		// yyyy-MM-ddTHH:mm:ss.MMMMMMZ
		// yyyy-MM-ddTHH:mm:ss.MMMMMM+HH:MM
		// 012345678901234567890123
		String year = value.substring(0, 4);
		String month = value.substring(5, 7);
		String day = value.substring(8, 10);
		String hour = value.substring(11, 13);
		String minute = value.substring(14, 16);
		String seconds = value.substring(17, 19);
		int milliPos = value.lastIndexOf('.');
		int zonePos = value.lastIndexOf('+');
		if (zonePos == -1) {
			zonePos = value.lastIndexOf('Z');
		}
		String millis = "0";
		String zone;
		Calendar calendar;
		if (zonePos == -1) {
			if (milliPos != -1) {
				millis = value.substring(milliPos + 1);
			}
			calendar = Calendar.getInstance();
		} else {
			if (milliPos != -1) {
				millis = value.substring(milliPos + 1, Math.min(zonePos, 23));
			}
			zone = value.substring(zonePos);
			TimeZone timeZone;
			if (zone.equals("Z")) {
				timeZone = TimeZone.getTimeZone("GMT");
			} else {
				timeZone = TimeZone.getTimeZone("GMT" + zone);
			}
			calendar = Calendar.getInstance(timeZone);
		}
		calendar.set(Calendar.YEAR, Integer.parseInt(year));
		calendar.set(Calendar.MONTH, Integer.parseInt(month) - 1);
		calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(day));
		calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
		calendar.set(Calendar.MINUTE, Integer.parseInt(minute));
		calendar.set(Calendar.SECOND, Integer.parseInt(seconds));
		calendar.set(Calendar.MILLISECOND, Integer.parseInt(millis));
		return calendar.getTime();
	}

	/**
	 * Set the current state of the XML processing
	 * 
	 * @param state
	 *            The state (<code>State</code>) parameter.
	 */
	private void setState(final XMLConverterContentHandler.State state) {
		this.state = state;
	}

	/**
	 * Start document.
	 */
	@Override
	public void startDocument() {
		this.state.startDocument();
	}

	/**
	 * Start element with the specified namespace uri, local name, q name and
	 * atts parameters.
	 * 
	 * @param namespaceURI
	 *            The namespace uri (<code>String</code>) parameter.
	 * @param localName
	 *            The local name (<code>String</code>) parameter.
	 * @param qName
	 *            The q name (<code>String</code>) parameter.
	 * @param atts
	 *            The atts (<code>Attributes</code>) parameter.
	 * @throws SAXException
	 *             SAXException.
	 * @see #startElement(String, String, String, Attributes)
	 */
	@Override
	public void startElement(final String namespaceURI, final String localName, final String qName, final Attributes atts)
			throws SAXException {
		this.state.startElement(localName, atts);
	}

}
