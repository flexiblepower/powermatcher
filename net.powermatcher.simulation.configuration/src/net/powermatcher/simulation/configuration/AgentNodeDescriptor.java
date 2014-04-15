package net.powermatcher.simulation.configuration;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "agent")
public class AgentNodeDescriptor<C extends NodeDescriptor<?>> extends NodeDescriptor<C> {
}
