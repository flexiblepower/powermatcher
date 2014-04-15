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
 * The primary key class for the AUTHORIZATION database table.
 * 
 */
@Embeddable
public class AuthorizationPK implements Serializable {
	//default serial version id, required for serializable classes.
	private static final long serialVersionUID = 1L;

	private String userid;

	private String nodeid;

    public AuthorizationPK() {
    }
	public String getUserid() {
		return this.userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getNodeid() {
		return this.nodeid;
	}
	public void setNodeid(String nodeid) {
		this.nodeid = nodeid;
	}

	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AuthorizationPK)) {
			return false;
		}
		AuthorizationPK castOther = (AuthorizationPK)other;
		return 
			this.userid.equals(castOther.userid)
			&& this.nodeid.equals(castOther.nodeid);

    }
    
	public int hashCode() {
		final int prime = 31;
		int hash = 17;
		hash = hash * prime + this.userid.hashCode();
		hash = hash * prime + this.nodeid.hashCode();
		
		return hash;
    }
}