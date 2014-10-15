package net.powermatcher.simulation.telemetry.metadata.component;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.powermatcher.simulation.telemetry.metadata.ProviderDefinition;
import net.powermatcher.simulation.telemetry.metadata.TelemetryMetaData;
import net.powermatcher.simulation.telemetry.metadata.TelemetryMetaDataService;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TelemetryMetaDataServiceImpl implements BundleActivator, TelemetryMetaDataService {
	private static Logger logger = LoggerFactory.getLogger(TelemetryMetaDataServiceImpl.class);

	private Map<Bundle, TelemetryMetaData> metaData = new HashMap<Bundle, TelemetryMetaData>();

	private TelemetryMetaDataParser parser = new TelemetryMetaDataParser();

	private ServiceRegistration serviceRegistration;

	@Override
	public void start(BundleContext context) throws Exception {
		serviceRegistration = context.registerService(TelemetryMetaDataService.class.getName(), this, null);

		for (Bundle bundle : context.getBundles()) {
			parseTelemetryMetaData(bundle);
		}

		context.addBundleListener(new BundleListener() {
			public void bundleChanged(BundleEvent event) {
				switch (event.getType()) {
				case BundleEvent.INSTALLED:
					parseTelemetryMetaData(event.getBundle());
					break;
				case BundleEvent.UNINSTALLED:
					metaData.remove(event.getBundle());
					break;
				}
			}
		});
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (serviceRegistration != null) {
			serviceRegistration.unregister();
			serviceRegistration = null;
		}
	}

	@Override
	public TelemetryMetaData getTelemetryMetaData(Bundle bundle) {
		return metaData.get(bundle);
	}

	private void parseTelemetryMetaData(Bundle bundle) {
		if (bundle.getHeaders().get(Constants.FRAGMENT_HOST) != null) {
			return;
		}

		@SuppressWarnings("unchecked")
		Enumeration<URL> docs = bundle.findEntries(DOCUMENTS_LOCATION, "*.xml", false);
		if (docs == null || !docs.hasMoreElements()) {
			return;
		}

		List<ProviderDefinition> providerDefinitions = new ArrayList<ProviderDefinition>();

		while (docs.hasMoreElements()) {
			URL doc = docs.nextElement();

			try {
				ProviderDefinition providerDefinition = parser.parse(doc);
				if (providerDefinition != null) {
					providerDefinitions.add(providerDefinition);
				}
			} catch (IOException e) {
				logger.warn("Could not read telemetry metadata from " + doc, e);
			}
		}

		if (providerDefinitions.size() > 0) {
			TelemetryMetaData telemetryMetaData = new TelemetryMetaData(
					providerDefinitions.toArray(new ProviderDefinition[providerDefinitions.size()]));
			metaData.put(bundle, telemetryMetaData);
			logger.info("Added telemetry meta data for bundle {}", bundle);
		}
	}
}
