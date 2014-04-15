package net.powermatcher.core.logging.bridge;


import org.osgi.framework.ServiceReference;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

/**
 * @author IBM
 * @version 0.9.0
 */
@Component(name = LoggingBridge.COMPONENT_NAME)
public class LoggingBridge {
	/**
	 * Define the component name (String) constant.
	 */
	public final static String COMPONENT_NAME = "net.powermatcher.core.logging.bridge.LoggingBridge";

	/**
	 * Create marker with the specified bundle name and service reference
	 * parameters and return the Marker result.
	 * 
	 * @param bundleName
	 *            The bundle name (<code>String</code>) parameter.
	 * @param serviceReference
	 *            The service reference (<code>ServiceReference</code>)
	 *            parameter.
	 * @return Results of the create marker (<code>Marker</code>) value.
	 */
	private static Marker createMarker(final String bundleName, final ServiceReference serviceReference) {
		Marker marker = MarkerFactory.getMarker(bundleName);
		if (serviceReference != null) {
			marker.add(MarkerFactory.getMarker(serviceReference.toString()));
		}
		return marker;
	}

	/**
	 * Log with the specified logger, level, marker, message and t parameters.
	 * 
	 * @param logger
	 *            The logger (<code>Logger</code>) parameter.
	 * @param level
	 *            The level (<code>int</code>) parameter.
	 * @param marker
	 *            The marker (<code>Marker</code>) parameter.
	 * @param message
	 *            The message (<code>String</code>) parameter.
	 * @param t
	 *            The t (<code>Throwable</code>) parameter.
	 */
	private static void log(final Logger logger, final int level, final Marker marker, final String message, final Throwable t) {
		switch (level) {
		case LogService.LOG_DEBUG:
			logger.debug(marker, message, t);
			break;
		case LogService.LOG_ERROR:
			logger.error(marker, message, t);
			break;
		case LogService.LOG_WARNING:
			logger.warn(marker, message, t);
			break;
		default:
			logger.info(marker, message, t);
			break;
		}
	}

	/**
	 * Define the listener (LogListener) field.
	 */
	private LogListener listener = new LogListener() {
		@Override
		public void logged(final LogEntry entry) {
			String bundleName = entry.getBundle().getSymbolicName();
			Marker marker = createMarker(bundleName, entry.getServiceReference());
			Logger logger = LoggerFactory.getLogger(bundleName);
			log(logger, entry.getLevel(), marker, entry.getMessage(), entry.getException());
		}
	};

	/**
	 * Sets the log reader service value.
	 * 
	 * @param logReader
	 *            The log reader (<code>LogReaderService</code>) parameter.
	 * @see #unsetLogReaderService(LogReaderService)
	 */
	@Reference
	public void setLogReaderService(final LogReaderService logReader) {
		logReader.addLogListener(this.listener);
	}

	/**
	 * Unset log reader service with the specified log reader parameter.
	 * 
	 * @param logReader
	 *            The log reader (<code>LogReaderService</code>) parameter.
	 * @see #setLogReaderService(LogReaderService)
	 */
	public void unsetLogReaderService(final LogReaderService logReader) {
		logReader.removeLogListener(this.listener);
	}

}
