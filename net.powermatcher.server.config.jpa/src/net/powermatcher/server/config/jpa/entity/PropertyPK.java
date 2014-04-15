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

/**
 * The primary key class for the PROPERTY database table.
 * 
 */
@Embeddable
public class PropertyPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	@Column(name="CLUSTER_ID")
	private String clusterId;

	@Column(name="CONFIG_ID")
	private String configId;

	private String name;

    public PropertyPK() {
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
	public String getName() {
		return this.name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PropertyPK)) {
			return false;
		}
		PropertyPK castOther = (PropertyPK)other;
		return 
			this.clusterId.equals(castOther.clusterId)
			&& this.configId.equals(castOther.configId)
			&& this.name.equals(castOther.name);

    }
    
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.clusterId.hashCode();
		hash = hash * prime + this.configId.hashCode();
		hash = hash * prime + this.name.hashCode();
		
		return hash;
    }
}