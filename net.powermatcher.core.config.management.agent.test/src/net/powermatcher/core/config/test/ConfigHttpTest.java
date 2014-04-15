package net.powermatcher.core.config.test;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.codec.binary.Base64;
import org.junit.Before;
import org.junit.Test;

/**
 * @author IBM
 * @version 0.9.0
 */
public class ConfigHttpTest {

	private String urlConfigString = "http://localhost:9080/configservice/nodeconfiguration";
	private String nodeId = "Development";
	private String userid = "configuser";
	private String password = "password";
	private String encoding = "UTF-8";

	private InputStream httpGet(final String urlString, final String encoding, final String username, final String password)
			throws IOException {
		System.out.println("url string:" + urlString); // DEBUG
		URL url = new URL(urlString);

		// Build parameter string
		String data = "nodeid=" + this.nodeId;

		// Send data
		URLConnection conn = url.openConnection();
		conn.setConnectTimeout(3000);
		conn.setReadTimeout(3000);

		// Userid and password for basic authentication
		String userpass = username + ":" + password;
		String basicAuth = "Basic " + new String(new Base64().encode(userpass.getBytes()));
		conn.setRequestProperty("Authorization", basicAuth);

		conn.setDoOutput(true);
		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());

		// Writer
		wr.write(data);
		wr.flush();
		wr.close();

		// Get the response
		return conn.getInputStream();
	}

	/**
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception {

	}

	/**
	 * @throws Exception
	 */
	@Test
	public void testGetConfigurationThroughHttp() throws Exception {
		InputStream in = null;
		try {
			in = this.httpGet(this.urlConfigString, this.encoding, this.userid, this.password);

			BufferedReader rd = new BufferedReader(new InputStreamReader(in));
			String line;
			while ((line = rd.readLine()) != null) {
				System.out.println(line);
			}
			System.out.println("Ready");
			// ConfigUpdateRequest request = this.parse(in);
			// System.out.println(request);
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Error parsing configuration.", e);
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	// private void getConfig() {
	// try {
	// // Construct data
	// String data = URLEncoder.encode("key1", "UTF-8") + "=" +
	// URLEncoder.encode("value1", "UTF-8");
	// data += "&" + URLEncoder.encode("key2", "UTF-8") + "=" +
	// URLEncoder.encode("value2", "UTF-8");
	//
	// // Send data
	// URL url = new URL("http://hostname:80/cgi");
	// URLConnection conn = url.openConnection();
	// conn.setDoOutput(true);
	// OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
	// wr.write(data);
	// wr.flush();
	//
	// // Get the response
	// BufferedReader rd = new BufferedReader(new
	// InputStreamReader(conn.getInputStream()));
	// String line;
	// while ((line = rd.readLine()) != null) {
	// // Process line...
	// }
	// wr.close();
	// rd.close();
	// } catch (Exception e) {
	// }
	// }

}
