package net.powermatcher.der.agent.miele.at.home;


import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author IBM
 * @version 0.9.0
 */
public class HttpUtils {

	private final static Logger logger = LoggerFactory.getLogger(HttpUtils.class);
	private static final int CONNECTION_TIME_OUT = 3000;
	private static final int READ_TIME_OUT = 3000;

	/**
	 * @param urlString
	 * @param encoding
	 * @return TODO
	 * @throws HttpUtilException
	 */
	public static InputStream httpGet(final String urlString, final String encoding) throws HttpUtilException {

		try {
			URL url = new URL(urlString);

			// Send data
			URLConnection conn = url.openConnection();
			conn.setConnectTimeout(CONNECTION_TIME_OUT);
			conn.setReadTimeout(READ_TIME_OUT);

			// Get the response
			return conn.getInputStream();

		} catch (Exception e) {
			String msg = "Error performing http get operation. " + e.getMessage();
			logger.error(msg, e);
			throw new HttpUtilException("Http get operation failed. ");
		}
	}

	/**
	 * @param endpoint
	 * @param requestParameters
	 * @param encoding
	 * @return TODO
	 * @throws HttpUtilException
	 */
	public static InputStream httpGet(final String endpoint, final String requestParameters, final String encoding)
			throws HttpUtilException {
		// Construct URL string
		String urlStr = endpoint;
		if (requestParameters != null && requestParameters.length() > 0) {
			urlStr += "?" + requestParameters;
		}

		// Get the response
		return httpGet(urlStr, encoding);
	}

}
