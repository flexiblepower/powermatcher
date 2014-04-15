package net.powermatcher.server.config.jpa.entity;

/********************************************
 * Copyright (c) 2012, 2013 Alliander.      *
 * All rights reserved.                     *
 *                                          *
 * Contributors:                            *
 *     IBM - initial API and implementation *
 *******************************************/

import java.io.Serializable;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;


/**
 * The persistent class for the CONFIGURATION database table.
 * 
 */
@Entity
public class Configuration implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum ConfigurationType { factory, singleton, group}
	
	@EmbeddedId
	private ConfigurationPK id;

	private String pid;

	private boolean template;

	private ConfigurationType type;

	//bi-directional many-to-one association to Configuration
	@XmlAttribute @XmlIDREF
    @ManyToOne
	@JoinColumns({
		@JoinColumn(name="PARENT_CLUSTER_ID", referencedColumnName="CLUSTER_ID"),
		@JoinColumn(name="PARENT_CONFIG_ID", referencedColumnName="CONFIG_ID")
		})
	private Configuration parent;

	//bi-directional many-to-one association to Configuration
	@OneToMany(mappedBy="parent", fetch=FetchType.EAGER, cascade=CascadeType.REFRESH)
	private Set<Configuration> configurations;

	//bi-directional many-to-one association to Property
	@OneToMany(mappedBy="configuration", fetch=FetchType.EAGER)
	private Set<Property> properties;

    public Configuration() {
    }

	public ConfigurationPK getId() {
		return this.id;
	}

	public void setId(ConfigurationPK id) {
		this.id = id;
	}
	
	public String getPid() {
		return this.pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public boolean isTemplate() {
		return this.template;
	}

	public void setTemplate(boolean template) {
		this.template = template;
	}

	public ConfigurationType getType() {
		return this.type;
	}

	public void setType(ConfigurationType type) {
		this.type = type;
	}

	public Configuration getParent() {
		return this.parent;
	}

	public void setParent(Configuration parent) {
		this.parent = parent;
	}
	
	public Set<Configuration> getConfigurations() {
		return this.configurations;
	}

	public void setConfigurations(Set<Configuration> configurations) {
		this.configurations = configurations;
	}
	
	public Set<Property> getProperties() {
		return this.properties;
	}

	public void setProperties(Set<Property> properties) {
		this.properties = properties;
	}
	
}