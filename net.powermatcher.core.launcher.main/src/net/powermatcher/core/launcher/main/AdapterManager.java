package net.powermatcher.core.launcher.main;


import java.util.Set;

import net.powermatcher.core.adapter.service.Adaptable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author IBM
 * @version 0.9.0
 */
public class AdapterManager {
	/**
	 * Define the logger (Logger) field.
	 */
	private final static Logger logger = LoggerFactory.getLogger(AdapterManager.class);
	/**
	 * Define the adapters (List<AbstractAgent>) field.
	 */
	private Set<Adaptable> adapters;

	/**
	 * Constructs an instance of this class.
	 */
	public AdapterManager() {
	}

	/**
	 * Sets the adapters value.
	 * 
	 * @param adapters
	 *            The adapters (<code>AbstractAgent[]</code>) parameter.
	 */
	public void setAdapters(final Set<Adaptable> adapters) {
		this.adapters = adapters;
	}

	/**
	 * Start.
	 * 
	 * @throws Exception
	 *             Exception.
	 */
	public void start() throws Exception {
		for (Adaptable adapter : this.adapters) {
			if (adapter.isEnabled()) {
				logger.info("Starting " + adapter.getName() + ": " + adapter.getId());
				adapter.bind();
			} else {
				logger.info("Skipping disabled " + adapter.getName() + ": " + adapter.getId());
			}
		}
	}

	/**
	 * Stop.
	 * 
	 * @throws Exception
	 *             Exception.
	 */
	public void stop() throws Exception {
		for (Adaptable adapter : this.adapters) {
			if (adapter.isEnabled()) {
				logger.info("Stopping " + adapter.getName() + ": " + adapter.getId());
				adapter.unbind();
			}
		}
	}

}
