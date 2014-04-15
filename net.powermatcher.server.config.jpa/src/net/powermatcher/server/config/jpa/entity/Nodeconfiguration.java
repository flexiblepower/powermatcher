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

import net.powermatcher.server.config.jpa.entity.Configuration;

import java.util.List;


/**
 * The persistent class for the NODECONFIGURATION database table.
 * 
 */
@Entity
public class Nodeconfiguration implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private String nodeid;

	private String description;

	private String name;

	private short updated;

	@OneToMany(fetch=FetchType.EAGER)
	private List<Configuration> configurationList;

    public Nodeconfiguration() {
    }

	public String getNodeid() {
		return this.nodeid;
	}

	public void setNodeid(String nodeid) {
		this.nodeid = nodeid;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public short getUpdated() {
		return this.updated;
	}

	public void setUpdated(short updated) {
		this.updated = updated;
	}

	public List<Configuration> getConfigurationList() {
		return this.configurationList;
	}

	public void setConfigurationList(List<Configuration> configurationList) {
		this.configurationList = configurationList;
	}

}