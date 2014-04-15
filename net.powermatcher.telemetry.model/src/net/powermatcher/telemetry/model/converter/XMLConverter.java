package net.powermatcher.telemetry.model.converter;


import java.io.StringReader;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.powermatcher.telemetry.model.constants.TelemetryModelConstants;
import net.powermatcher.telemetry.model.data.TelemetryData;

import org.xml.sax.InputSource;


/**
 * @author IBM
 * @version 0.9.0
 */
public class XMLConverter implements TelemetryModelConstants {
	/**
	 * Append date with the specified calendar and buffer parameters.
	 * 
	 * @param calendar
	 *            The calendar (<code>Calendar</code>) parameter.
	 * @param buffer
	 *            The buffer (<code>StringBuffer</code>) parameter.
	 * @see #formatISO8601Date(Date)
	 */
	private static void appendDate(final Calendar calendar, final StringBuffer buffer) {
		int year = calendar.get(Calendar.YEAR);
		buffer.append(year);
		buffer.append('-');
		int month = calendar.get(Calendar.MONTH) + 1;
		if (month < 10) {
			buffer.append('0');
		}
		buffer.append(month);
		buffer.append('-');
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		if (day < 10) {
			buffer.append('0');
		}
		buffer.append(day);
	}

	/**
	 * Append time with the specified calendar and buffer parameters.
	 * 
	 * @param calendar
	 *            The calendar (<code>Calendar</code>) parameter.
	 * @param buffer
	 *            The buffer (<code>StringBuffer</code>) parameter.
	 */
	private static void appendTime(final Calendar calendar, final StringBuffer buffer) {
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		if (hour < 10) {
			buffer.append('0');
		}
		buffer.append(hour);
		buffer.append(':');
		int minute = calendar.get(Calendar.MINUTE);
		if (minute < 10) {
			buffer.append('0');
		}
		buffer.append(minute);
		buffer.append(':');
		int second = calendar.get(Calendar.SECOND);
		if (second < 10) {
			buffer.append('0');
		}
		buffer.append(second);
		buffer.append('.');
		int millisecond = calendar.get(Calendar.MILLISECOND);
		if (millisecond < 100) {
			buffer.append('0');
		}
		if (millisecond < 10) {
			buffer.append('0');
		}
		buffer.append(millisecond);
	}

	/**
	 * Append time zone with the specified calendar and buffer parameters.
	 * 
	 * @param calendar
	 *            The calendar (<code>Calendar</code>) parameter.
	 * @param buffer
	 *            The buffer (<code>StringBuffer</code>) parameter.
	 */
	private static void appendTimeZone(final Calendar calendar, final StringBuffer buffer) {
		long offset = calendar.getTimeZone().getRawOffset();
		long hour = (offset < 0 ? -offset : offset) / 3600000;
		long minute = ((offset < 0 ? -offset : offset) % 3600000) / 60000;
		if (hour == 0 && minute == 0) {
			buffer.append("Z");
			return;
		}
		buffer.append(offset < 0 ? '-' : '+');
		if (hour < 10) {
			buffer.append('0');
		}
		buffer.append(hour);
		buffer.append(':');
		if (minute < 10) {
			buffer.append('0');
		}
		buffer.append(minute);
	}

	/**
	 * End element with the specified buffer and elem name parameters.
	 * 
	 * @param buffer
	 *            The buffer (<code>StringBuffer</code>) parameter.
	 * @param elemName
	 *            The elem name (<code>String</code>) parameter.
	 */
	private static void endElement(final StringBuffer buffer, final String elemName) {
		buffer.append("</");
		buffer.append(elemName);
		buffer.append('>');
		buffer.append('\n');
	}

	/**
	 * Format iso8601 date with the specified date parameter and return the
	 * String result.
	 * 
	 * @param date
	 *            The date (<code>Date</code>) parameter.
	 * @return Results of the format iso8601 date (<code>String</code>) value.
	 */
	public static String formatISO8601Date(final Date date) {
		// yyyy-MM-ddTHH:mm:ss.MMMZ
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
		calendar.setTime(date);
		StringBuffer buffer = new StringBuffer(29);
		appendDate(calendar, buffer);
		buffer.append("T");
		appendTime(calendar, buffer);
		appendTimeZone(calendar, buffer);
		return buffer.toString();
	}

	/**
	 * Start element with the specified buffer, elem name and map parameters and
	 * return the int result.
	 * 
	 * @param buffer
	 *            The buffer (<code>StringBuffer</code>) parameter.
	 * @param elemName
	 *            The elem name (<code>String</code>) parameter.
	 * @param map
	 *            The map (<code>Map<String,Object></code>) parameter.
	 * @return Results of the start element (<code>int</code>) value.
	 */
	private static int startElement(final StringBuffer buffer, final String elemName, final Map<String, Object> map) {
		int nestedElementCount = 0;
		buffer.append('<');
		buffer.append(elemName);
		for (Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext();) {
			Object key = iterator.next();
			Object value = map.get(key);
			if (value instanceof Map) {
				nestedElementCount += 1;
			} else if (value != null) {
				buffer.append(' ');
				buffer.append(key);
				buffer.append("=\"");
				if (value instanceof Date) {
					buffer.append(formatISO8601Date((Date) value));
				} else {
					buffer.append(value);
				}
				buffer.append('"');
			}
		}
		if (nestedElementCount == 0) {
			buffer.append("/>");
		} else {
			buffer.append('>');
		}
		buffer.append('\n');
		return nestedElementCount;
	}

	/**
	 * To telemetry data with the specified XML parameter and return the
	 * TelemetryData result.
	 * 
	 * @param xml
	 *            The XML (<code>String</code>) parameter.
	 * @return Results of the to data (<code>TelemetryData</code>) value.
	 * @throws Exception
	 *             Exception.
	 */
	public static TelemetryData toData(final String xml) throws Exception {
		Map<String, Object> dataMap = toMap(xml);
		return new TelemetryData(dataMap);
	}

	/**
	 * To map with the specified XML parameter and return the Map result.
	 * 
	 * @param xml
	 *            The XML (<code>String</code>) parameter.
	 * @return Results of the to map (<code>Map</code>) value.
	 * @throws Exception
	 *             Exception.
	 */
	public static Map<String, Object> toMap(final String xml) throws Exception {
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setValidating(false);
		spf.setNamespaceAware(true);
		SAXParser sp = spf.newSAXParser();
		XMLConverterContentHandler contentHandler = new XMLConverterContentHandler();
		InputSource is = new InputSource(new StringReader(xml));
		sp.parse(is, contentHandler);
		return contentHandler.getMap();
	}

	/**
	 * To xmlstring with the specified map parameter and return the String
	 * result.
	 * 
	 * @param map
	 *            The map (<code>Map<String,Object></code>) parameter.
	 * @return Results of the to xmlstring (<code>String</code>) value.
	 * @see #toXMLString(TelemetryData)
	 */
	public static String toXMLString(final Map<String, Object> map) {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		toXMLString(buffer, ELEM_TELEMETRY, map);
		return buffer.toString();
	}

	/**
	 * To xmlstring with the specified buffer, elem name and map parameters.
	 * 
	 * @param buffer
	 *            The buffer (<code>StringBuffer</code>) parameter.
	 * @param elemName
	 *            The elem name (<code>String</code>) parameter.
	 * @param map
	 *            The map (<code>Map<String,Object></code>) parameter.
	 * @see #toXMLString(Map)
	 * @see #toXMLString(TelemetryData)
	 */
	@SuppressWarnings("unchecked")
	private static void toXMLString(final StringBuffer buffer, final String elemName, final Map<String, Object> map) {
		int nestedElementCount = startElement(buffer, elemName, map);
		if (nestedElementCount != 0) {
			for (Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext();) {
				Object key = iterator.next();
				Object value = map.get(key);
				if (value instanceof Map) {
					String nestedElemName = (String) key;
					int endIndex = nestedElemName.lastIndexOf('_');
					if (endIndex > 0) {
						nestedElemName = nestedElemName.substring(0, endIndex);
					}
					toXMLString(buffer, nestedElemName, (Map<String, Object>) value);
				}
			}
			endElement(buffer, elemName);
		}
	}

	/**
	 * Encode telemetry data in XML.
	 * 
	 * @param data
	 *            The data (<code>TelemetryData</code>) parameter.
	 * @return Result of XML encoding of telemetry data.
	 * @see #toXMLString(Map)
	 */
	public static String toXMLString(final TelemetryData data) {
		return toXMLString(data.getDataMap());
	}

	/**
	 * Constructs an instance of this class.
	 */
	private XMLConverter() {
		super();
	}

}
