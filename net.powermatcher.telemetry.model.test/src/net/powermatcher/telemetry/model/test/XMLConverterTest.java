package net.powermatcher.telemetry.model.test;


import java.io.DataInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

import net.powermatcher.telemetry.model.converter.XMLConverter;

import junit.framework.TestCase;


/**
 * @author IBM
 * @version 0.9.0
 */
public class XMLConverterTest extends TestCase {
	/**
	 * Map to XML with the specified name parameter.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @throws Exception
	 *             Exception.
	 * @see #payloadToXml(ISensorEventPayload)
	 */
	public static void mapToXml(final String name) throws Exception {
		Map<String, Object> map = xmlToMap(name);
		String xml = XMLConverter.toXMLString(map);
		System.out.println(name + ": " + xml);
	}

	/**
	 * XML to map with the specified name parameter and return the Map result.
	 * 
	 * @param name
	 *            The name (<code>String</code>) parameter.
	 * @return Results of the XML to map (<code>Map</code>) value.
	 * @throws Exception
	 *             Exception.
	 * @see #test00_ToMap()
	 */
	public static Map<String, Object> xmlToMap(final String name) throws Exception {
		String resourceName = "TelemetrySchema-" + name + ".xml";
		InputStream is = new DataInputStream(XMLConverterTest.class.getResourceAsStream(resourceName));
		Reader in = new InputStreamReader(is, "UTF-8");
		final char[] buffer = new char[0x10000];
		StringBuffer out = new StringBuffer();
		int read;
		do {
			read = in.read(buffer, 0, buffer.length);
			if (read > 0) {
				out.append(buffer, 0, read);
			}
		} while (read >= 0);
		Map<String, Object> map = XMLConverter.toMap(out.toString());
		return map;
	}

	/**
	 * Test00_ to map.
	 * 
	 * @throws Exception
	 *             Exception.
	 * @see #xmlToMap(String)
	 */
	public void test00_ToMap() throws Exception {
		xmlToMap("Alert");
		xmlToMap("Measurement");
		xmlToMap("Status");
		xmlToMap("Control");
		xmlToMap("Request");
		xmlToMap("Request2");
		xmlToMap("Response");
		xmlToMap("Legacy");
	}

	/**
	 * Test01_ to xmlstring.
	 * 
	 * @throws Exception
	 *             Exception.
	 * @see #test02_ControlToXMLString()
	 * @see #test03_RequestToXMLString()
	 * @see #test04_ResponseToXMLString()
	 */
	public void test01_ToXMLString() throws Exception {
		mapToXml("Alert");
		mapToXml("Measurement");
		mapToXml("Status");
		mapToXml("Control");
		mapToXml("Request");
		mapToXml("Request2");
		mapToXml("Response");
		mapToXml("Legacy");
	}

}
