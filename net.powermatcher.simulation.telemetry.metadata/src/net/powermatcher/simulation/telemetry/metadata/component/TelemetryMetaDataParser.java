package net.powermatcher.simulation.telemetry.metadata.component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import net.powermatcher.simulation.telemetry.metadata.AlertTelemetryDefinition;
import net.powermatcher.simulation.telemetry.metadata.ControlTelemetryDefinition;
import net.powermatcher.simulation.telemetry.metadata.MeasurementTelemetryDefinition;
import net.powermatcher.simulation.telemetry.metadata.ProviderDefinition;
import net.powermatcher.simulation.telemetry.metadata.StatusTelemetryDefinition;
import net.powermatcher.simulation.telemetry.metadata.TelemetryDefinition;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParserException;

public class TelemetryMetaDataParser {
	public static final String NAMESPACE = "http://powermatcher.net/telemetry-metadata/";

	private static final String ATTRIBUTE_OPTIONS = "options";
	private static final String ATTRIBUTE_UNIT = "unit";
	private static final String ATTRIBUTE_DESCRIPTION = "description";
	private static final String ATTRIBUTE_KEY = "key";
	private static final String ATTRIBUTE_CLASS = "class";

	private static final String TAG_ALERT = "alert";
	private static final String TAG_CONTROL = "control";
	private static final String TAG_MEASUREMENT = "measurement";
	private static final String TAG_STATUS = "status";
	private static final String TAG_PROVIDER = "telemetry-provider";

	public ProviderDefinition parse(URL url) throws IOException {
		InputStream is = null;

		try {
			is = url.openStream();
			return this.parse(is);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}

	public ProviderDefinition parse(InputStream is) throws IOException {
		try {
			KXmlParser parser = new KXmlParser();
			// TODO check namespaces -- parser.setFeature(KXmlParser.FEATURE_PROCESS_NAMESPACES, true);
			parser.setInput(is, null);

			parser.nextTag();
			if (TAG_PROVIDER.equals(parser.getName()) == false) {
				throw new IOException("unsupported format, tag '" + TAG_PROVIDER + "' expected");
			}

			String providerClass = parser.getAttributeValue(null, ATTRIBUTE_CLASS);

			List<TelemetryDefinition> telemetryDefinitions = new ArrayList<TelemetryDefinition>();
			while (parser.nextTag() == KXmlParser.START_TAG) {
				telemetryDefinitions.add(parseTelemetryDefinition(parser));
			}

			TelemetryDefinition[] telemetryDefinitionsArray = telemetryDefinitions
					.toArray(new TelemetryDefinition[telemetryDefinitions.size()]);
			return new ProviderDefinition(providerClass, telemetryDefinitionsArray);
		} catch (XmlPullParserException e) {
			throw new IOException(e);
		}
	}

	private TelemetryDefinition parseTelemetryDefinition(KXmlParser parser) throws XmlPullParserException, IOException {
		String telemetryDefinitionType = parser.getName();

		TelemetryDefinition telemetryDefinition = null;

		if (TAG_ALERT.equals(telemetryDefinitionType)) {
			telemetryDefinition = parseAlertTelemetryDefinition(parser);
		} else if (TAG_CONTROL.equals(telemetryDefinitionType)) {
			telemetryDefinition = parseControlTelemetryDefinition(parser);
		} else if (TAG_MEASUREMENT.equals(telemetryDefinitionType)) {
			telemetryDefinition = parseMeasurementTelemetryDefinition(parser);
		} else if (TAG_STATUS.equals(telemetryDefinitionType)) {
			telemetryDefinition = parseStatusTelemetryDefinition(parser);
		} else {
			throw new IOException("unsupported telemetry definition: " + telemetryDefinitionType + " at line number "
					+ parser.getLineNumber());
		}

		while (true) {
			if (parser.nextTag() == KXmlParser.END_TAG && parser.getName().equals(telemetryDefinitionType)) {
				break;
			}
		}

		return telemetryDefinition;
	}

	private TelemetryDefinition parseAlertTelemetryDefinition(KXmlParser parser) {
		String key = parser.getAttributeValue(null, ATTRIBUTE_KEY);
		String description = parser.getAttributeValue(null, ATTRIBUTE_DESCRIPTION);
		return new AlertTelemetryDefinition(key, description);
	}

	private TelemetryDefinition parseControlTelemetryDefinition(KXmlParser parser) {
		String key = parser.getAttributeValue(null, ATTRIBUTE_KEY);
		String description = parser.getAttributeValue(null, ATTRIBUTE_DESCRIPTION);
		String unit = parser.getAttributeValue(null, ATTRIBUTE_UNIT);
		return new ControlTelemetryDefinition(key, description, unit);
	}

	private TelemetryDefinition parseMeasurementTelemetryDefinition(KXmlParser parser) {
		String key = parser.getAttributeValue(null, ATTRIBUTE_KEY);
		String description = parser.getAttributeValue(null, ATTRIBUTE_DESCRIPTION);
		String unit = parser.getAttributeValue(null, ATTRIBUTE_UNIT);
		return new MeasurementTelemetryDefinition(key, description, unit);
	}

	private TelemetryDefinition parseStatusTelemetryDefinition(KXmlParser parser) {
		String key = parser.getAttributeValue(null, ATTRIBUTE_KEY);
		String description = parser.getAttributeValue(null, ATTRIBUTE_DESCRIPTION);
		String options = parser.getAttributeValue(null, ATTRIBUTE_OPTIONS);
		return new StatusTelemetryDefinition(key, description, options.split(","));
	}

	public static void main(String[] args) throws IOException {
		String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<telemetry-provider xmlns=\"http://powermatcher.net/telemetry-metadata/\"\n"
				+ "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
				+ "  xsi:schemaLocation=\"http://powermatcher.net/telemetry-metadata/ telemetry.xsd\"\n"
				+ "  class=\"net.powermatcher.simulation.models.RefridgeratorModel\">\n"
				+ "    <measurement key=\"temperature\" description=\"temperature of the refridgerator\" unit=\"C\" />\n"
				+ "    <status key=\"compressor\" description=\"compressor state\" options=\"on,off\" />\n"
				+ "    <control key=\"temperature-setpoint\" description=\"target temperature\" unit=\"C\" />\n"
				+ "    <alert key=\"max-temperature-exceeded\" description=\"temperature to high\" />\n"
				+ "</telemetry-provider>\n";

		System.out.println("parsing: ");
		System.out.println(input);
		System.out.println(new TelemetryMetaDataParser().parse(new ByteArrayInputStream(input.getBytes())));
	}
}
