package net.powermatcher.server.config.jpa.entity;

/********************************************
 * Copyright (c) 2012, 2013 Alliander.      *
 * All rights reserved.                     *
 *                                          *
 * Contributors:                            *
 *     IBM - initial API and implementation *
 *******************************************/

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;


/**
 * The persistent class for the PROPERTY database table.
 * 
 */
@Entity
public class Property implements Serializable {
	private static final long serialVersionUID = 1L;

	public enum PropertyType { String, Boolean, Integer, Float, Double, Long, Short}
	
	@EmbeddedId
	private PropertyPK id;

	private String value;

	//bi-directional many-to-one association to Configuration
    @ManyToOne
	@JoinColumns({
		@JoinColumn(name="CLUSTER_ID", referencedColumnName="CLUSTER_ID"),
		@JoinColumn(name="CONFIG_ID", referencedColumnName="CONFIG_ID")
		})
	private Configuration configuration;

    private PropertyType type;
    
    public Property() {
    }

	public PropertyPK getId() {
		return this.id;
	}

	public void setId(PropertyPK id) {
		this.id = id;
	}
	
	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Configuration getConfiguration() {
		return this.configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	
	public PropertyType getType() {
		return type;
	}

	public void setType(PropertyType type) {
		this.type = type;
	}
}