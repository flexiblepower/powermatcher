package net.powermatcher.server.config.jpa.entity;

/********************************************
 * Copyright (c) 2012, 2013 Alliander.      *
 * All rights reserved.                     *
 *                                          *
 * Contributors:                            *
 *     IBM - initial API and implementation *
 *******************************************/

import java.io.Serializable;
import javax.persistence.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;

/**
 * The primary key class for the CONFIGURATION database table.
 * 
 */
@Embeddable
public class ConfigurationPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@XmlAttribute @XmlID
	@Column(name="CLUSTER_ID")
	private String clusterId;

	@XmlAttribute @XmlID
	@Column(name="CONFIG_ID")
	private String configId;

    public ConfigurationPK() {
    }
	public String getClusterId() {
		return this.clusterId;
	}
	public void setClusterId(String clusterId) {
		this.clusterId = clusterId;
	}
	public String getConfigId() {
		return this.configId;
	}
	public void setConfigId(String configId) {
		this.configId = configId;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof ConfigurationPK)) {
			return false;
		}
		ConfigurationPK castOther = (ConfigurationPK)other;
		return 
			this.clusterId.equals(castOther.clusterId)
			&& this.configId.equals(castOther.configId);

    }
    
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.clusterId.hashCode();
		hash = hash * prime + this.configId.hashCode();
		
		return hash;
    }
}