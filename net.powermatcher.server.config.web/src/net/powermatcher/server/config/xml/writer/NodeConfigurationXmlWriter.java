package net.powermatcher.server.config.xml.writer;


import java.io.Writer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import net.powermatcher.server.config.jpa.entity.Configuration;
import net.powermatcher.server.config.jpa.entity.Nodeconfiguration;
import net.powermatcher.server.config.jpa.entity.Property;
import net.powermatcher.server.config.jpa.entity.Configuration.ConfigurationType;


public class NodeConfigurationXmlWriter {

	/**
	 * Define the logger (Logger) field.
	 */
	protected final static Logger logger = Logger.getLogger(NodeConfigurationXmlWriter.class.getName());

	/**
	 * Define the xml elements and attributes
	 */
	private static final String ELEMENT_NODE_CONFIGURATION = "nodeconfig";
	private static final String ELEMENT_CONFIGURATION = "configuration";
	private static final String ELEMENT_PROPERTY = "property";
	private static final String ATTRIBUTE_PID = "pid";
	private static final String ATTRIBUTE_CLUSTER = "cluster";
	private static final String ATTRIBUTE_TYPE = "type";
	private static final String ATTRIBUTE_DATE = "date";
	private static final String ATTRIBUTE_DESCRIPTION = "description";
	private static final String ATTRIBUTE_NAME = "name";
	private static final String ATTRIBUTE_ID = "id";
	private static final String ATTRIBUTE_TEMPLATE = "template";
	private static final String ATTRIBUTE_VALUE = "value";
	
	private static final String CONFIGURATION_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

		
	private Nodeconfiguration nodeconfig;

	public Nodeconfiguration getNodeconfig() {
		return nodeconfig;
	}

	public void setNodeconfig(Nodeconfiguration nodeconfig) {
		this.nodeconfig = nodeconfig;
	}
	
	public void write(Writer out) {
		try {
			XMLOutputFactory factory = XMLOutputFactory.newInstance();
		     XMLStreamWriter writer = factory.createXMLStreamWriter(out);

		     writer.writeStartDocument();
		     writeNodeConfiguration(writer);
		     writer.writeEndDocument();

		     writer.flush();
		     writer.close();

		 } catch (XMLStreamException e) {
			 logger.severe("Error creating XML output stream writer for node configuration.\n" + e.getStackTrace().toString());
		 }
	}
	
	public void write(Writer out, Nodeconfiguration nodeconfig) {
		this.setNodeconfig(nodeconfig);
		this.write(out);
	}
	
	private void writeNodeConfiguration(XMLStreamWriter writer) throws XMLStreamException {
		 writer.writeStartElement(ELEMENT_NODE_CONFIGURATION);
	     writer.writeAttribute(ATTRIBUTE_ID, nodeconfig.getNodeid());
	     writer.writeAttribute(ATTRIBUTE_NAME, nodeconfig.getName());
	     writer.writeAttribute(ATTRIBUTE_DESCRIPTION, nodeconfig.getDescription());
	     writer.writeAttribute(ATTRIBUTE_DATE, currentDate());
	     writeConfigurations(writer, new HashSet<Configuration>(nodeconfig.getConfigurationList()));
	     writer.writeEndElement();
	}
	

	
	/**
	 * Write the configurations and their properties including the parent configurations and
	 * their children. The method first finds the set of parent root configurations and starts
	 * from the root writing the configurations from the highest level and descending down the
	 * configuration tree.
	 * 
	 * @param writer				The writer object.
	 * @param configurations		The configurations to write.
	 * @throws XMLStreamException
	 */
	private void writeConfigurations(XMLStreamWriter writer, Set<Configuration> configurations)  throws XMLStreamException {
		 
		if (configurations != null) {
			Set<Configuration> rootSet = getRootConfigurations(configurations);
			for (Configuration config : rootSet) {
					writeConfiguration(writer, config);
			}
		}
	}

	
	/**
	 * Write the configurations and their properties including the parent configurations and
	 * their children. Configurations of type 'group' are ignored when they do not have child
	 * configurations.
	 * 
	 * @param writer				The writer object.
	 * @param configuration			The configuration to write.
	 * @throws XMLStreamException
	 */
	private void writeConfiguration(XMLStreamWriter writer, Configuration config) throws XMLStreamException {
		Set<Configuration> configurations = config.getConfigurations();
		
		// Do not write the configuration element in case it is a group without children.
		if (configurations == null && config.getType().equals(Configuration.ConfigurationType.group)) return;
			
		
		writer.writeStartElement(ELEMENT_CONFIGURATION);
	    writer.writeAttribute(ATTRIBUTE_TYPE, config.getType().toString());
	     
	    if (config.isTemplate()) writer.writeAttribute(ATTRIBUTE_TEMPLATE, new Boolean(config.isTemplate()).toString());
	    if (config.getParent() != null) writer.writeAttribute(ATTRIBUTE_CLUSTER, config.getId().getClusterId());
	    if (!config.getType().equals(ConfigurationType.group)) writer.writeAttribute(ATTRIBUTE_PID, config.getPid());
	    writer.writeAttribute(ATTRIBUTE_ID, config.getId().getConfigId());

	    writeProperties(writer, config.getProperties());
	    
	    // Write the child configurations
	    if (configurations != null) {
			for (Configuration configuration : configurations) {
				writeConfiguration(writer, configuration);
			}
		}   
	    
	    // Close the configuration element
		writer.writeEndElement();	
	}
	
	
	/**
	 * Returns the set of parent root configurations for the specified input configuration
	 * set.
	 * 
	 * @param configurations	The configuration set for which the parent root set has to be found.
	 * @return
	 */
	private Set<Configuration> getRootConfigurations(Set<Configuration> configurations)  {
		Set<Configuration> rootSet = new HashSet<Configuration>();
		if (configurations != null) {
			for (Configuration configuration : configurations) {
				Configuration root = configuration;
				while (root.getParent() != null) {
					root = root.getParent();
				}
				rootSet.add(root);
			}
		}
		return rootSet;
	}
	
	
	private void writeProperties(XMLStreamWriter writer, Set<Property> properties) throws XMLStreamException {
		
		if (properties != null) {
			for (Property property : properties) {
				writeProperty(writer, property);
			}
		}
	}
	
	private void writeProperty(XMLStreamWriter writer, Property property) throws XMLStreamException {
		 writer.writeStartElement(ELEMENT_PROPERTY);
		 writer.writeAttribute(ATTRIBUTE_NAME, property.getId().getName());
	     writer.writeAttribute(ATTRIBUTE_VALUE, property.getValue().toString());
	     writer.writeAttribute(ATTRIBUTE_TYPE, property.getType().toString());
	     writer.writeEndElement();
	}
	
	private String currentDate() {
		DateFormat format = new SimpleDateFormat(CONFIGURATION_DATE_FORMAT);
		return format.format(new Date());
	}

}
