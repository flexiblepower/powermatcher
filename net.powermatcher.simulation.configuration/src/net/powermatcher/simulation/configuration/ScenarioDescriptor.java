package net.powermatcher.simulation.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * Description of a complete scenario which can be saved, loaded and executed by the PowerMatcher Simulation Tool.
 * 
 * A scenario can contain multiple clusters.
 */
@XmlRootElement(name = "scenario")
@XmlType(propOrder = { "simulationClockDescriptor", "marketBasisDescriptor", "dataSinks", "children" })
// TODO add notifications to other change methods
public class ScenarioDescriptor extends ConfigurationElementImpl<ClusterDescriptor> {
	private static final AtomicLong scenarioIdSequence = new AtomicLong();

	private final List<DataSinkDescriptor> dataSinks = new ArrayList<DataSinkDescriptor>();

	private File file = null;

	private boolean isActive = false;

	/** The MarketBasis used in this scenario. A scenario has only one MarketBasis. */
	private MarketBasisDescriptor marketBasisDescriptor;

	private String scenarioId = "Scenario-" + scenarioIdSequence.incrementAndGet();

	/** The timing configuration for the scenario */
	private SimulationClockDescriptor simulationClockDescriptor;

	@Override
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public ConfigurationElement getParent() {
		return null;
	}

	public void addDataSink(DataSinkDescriptor sinkDescriptor) {
		this.dataSinks.add(sinkDescriptor);
	}

	/**
	 * Gets all the cluster descriptors in this Scenario
	 * 
	 * @return the cluster descriptors
	 */
	@Override
	@XmlElement(name = "cluster")
	@XmlElementWrapper(name = "clusters")
	public List<ClusterDescriptor> getChildren() {
		return super.getChildren();
	}

	@XmlAnyElement(lax = true)
	@XmlElementWrapper(name = "data_sinks")
	public List<DataSinkDescriptor> getDataSinks() {
		return this.dataSinks;
	}

	@XmlTransient
	public File getFile() {
		return this.file;
	}

	/**
	 * Gets the MarketBassis used in this scenario.
	 * 
	 * @return the MarketBasis
	 */
	@XmlElement(name = "market_basis")
	public MarketBasisDescriptor getMarketBasisDescriptor() {
		return this.marketBasisDescriptor;
	}

	@XmlAttribute(name = "id")
	public String getScenarioId() {
		return scenarioId;
	}

	@XmlElement(name = "timing_descriptor")
	public SimulationClockDescriptor getSimulationClockDescriptor() {
		return this.simulationClockDescriptor;
	}

	@XmlTransient
	public boolean isActive() {
		return isActive;
	}

	public boolean scenarioIsSaved() {
		return this.file != null;
	}

	public void setActive() {
		this.isActive = true;
	}

	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * Set the MarketBasis of this scenario.
	 * 
	 * @param marketBasisDescriptor
	 */
	public void setMarketBasisDescriptor(MarketBasisDescriptor marketBasisDescriptor) {
		this.marketBasisDescriptor = marketBasisDescriptor;
	}

	public void setPassive() {
		this.isActive = false;
	}

	public void setScenarioId(String scenarioId) {
		this.scenarioId = scenarioId;
	}

	public void setSimulationClockDescriptor(SimulationClockDescriptor timingDescriptor) {
		this.simulationClockDescriptor = timingDescriptor;
	}
}