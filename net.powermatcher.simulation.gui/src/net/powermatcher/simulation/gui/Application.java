package net.powermatcher.simulation.gui;

import net.powermatcher.simulation.engine.ComponentManager;
import net.powermatcher.simulation.engine.SimulationControl;
import net.powermatcher.simulation.gui.graph.GraphUpdater;
import net.powermatcher.simulation.logging.Broker;
import net.powermatcher.simulation.telemetry.metadata.TelemetryMetaDataService;

import org.eclipse.core.runtime.Assert;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.metatype.MetaTypeService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplication {
	private static Application application;

	public static Application getInstance() {
		Assert.isNotNull(application);
		return application;
	}

	private final ScenarioContainer scenarios = new ScenarioContainer();
	private final ApplicationSimulationControl applicationSimulationControl = new ApplicationSimulationControl();
	private final Broker broker = new Broker();

	private GraphUpdater graphUpdater;
	private Display display;

	private ServiceTracker<ConfigurationAdmin, ConfigurationAdmin> configurationAdminTracker;
	private ServiceTracker<MetaTypeService, MetaTypeService> metaTypeServiceTracker;
	private ServiceTracker<TelemetryMetaDataService, TelemetryMetaDataService> telemetryDataServiceTracker;

	private ComponentManager componentManager;

	public Broker getBroker() {
		return this.broker;
	}

	/**
	 * @return The ConfigurationAdmin if it is available in the OSGi container,
	 *         otherwise <code>null</code> is returned.
	 */
	public ConfigurationAdmin getConfigurationAdmin() {
		return this.configurationAdminTracker.getService();
	}

	public GraphUpdater getGraphUpdater() {
		return this.graphUpdater;
	}

	/**
	 * @return The MetaTypeService if it is available in the OSGi container,
	 *         otherwise <code>null</code> is returned.
	 */
	public MetaTypeService getMetaTypeService() {
		return this.metaTypeServiceTracker.getService();
	}

	public ScenarioContainer getScenarios() {
		return this.scenarios;
	}

	public SimulationControl getSimulationControl() {
		return this.applicationSimulationControl;
	}

	public TelemetryMetaDataService getTelemetryMetaDataService() {
		// TODO check if available ... ?
		return this.telemetryDataServiceTracker.getService();
	}

	@Override
	public Object start(IApplicationContext context) {
		application = this;

		BundleContext bundleContext = FrameworkUtil.getBundle(Application.class).getBundleContext();
		if (bundleContext == null) {
			System.err.println("Must start bundle before starting application");
			return IApplication.EXIT_OK;
		}

		// TODO check whether metaTypeServiceTracker and
		// configurationAdminTracker
		// can be removed in favor of the componentManager
		this.metaTypeServiceTracker = new ServiceTracker<MetaTypeService, MetaTypeService>(bundleContext,
				MetaTypeService.class, null);
		this.metaTypeServiceTracker.open();

		this.configurationAdminTracker = new ServiceTracker<ConfigurationAdmin, ConfigurationAdmin>(bundleContext,
				ConfigurationAdmin.class, null);
		this.configurationAdminTracker.open();

		this.telemetryDataServiceTracker = new ServiceTracker<TelemetryMetaDataService, TelemetryMetaDataService>(
				bundleContext, TelemetryMetaDataService.class, null);
		this.telemetryDataServiceTracker.open();

		this.componentManager = new ComponentManager(bundleContext);

		return startAndRunUI();
	}

	private Object startAndRunUI() {
		this.display = PlatformUI.createDisplay();

		this.graphUpdater = new GraphUpdater();
		this.graphUpdater.start(display);

		try {
			int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
			if (returnCode == PlatformUI.RETURN_RESTART) {
				return IApplication.EXIT_RESTART;
			}
			return IApplication.EXIT_OK;
		} finally {
			display.dispose();

			try {
				this.configurationAdminTracker.close();
			} catch (Exception e) {
			}

			try {
				this.metaTypeServiceTracker.close();
			} catch (Exception e) {
			}
			try {
				this.telemetryDataServiceTracker.close();
			} catch (Exception e) {

			}

			try {
				this.componentManager.close();
			} catch (Exception e) {
			}
		}
	}

	@Override
	public void stop() {
		if (!PlatformUI.isWorkbenchRunning())
			return;
		final IWorkbench workbench = PlatformUI.getWorkbench();
		final Display display = workbench.getDisplay();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				if (!display.isDisposed())
					workbench.close();
			}
		});
	}

	ComponentManager getComponentManager() {
		return componentManager;
	}

	public Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}

	public Display getDisplay() {
		return display;
	}
}
