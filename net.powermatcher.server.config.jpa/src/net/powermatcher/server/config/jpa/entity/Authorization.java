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
 * The persistent class for the AUTHORIZATION database table.
 * 
 */
@Entity
@NamedQueries({@NamedQuery(name = "getAuthorizationByNodeid", query = "SELECT a FROM Authorization a WHERE a.id.nodeid = :id_nodeid ORDER BY a.id.userid, a.id.nodeid"),@NamedQuery(name = "getAuthorizationByUserid", query = "SELECT a FROM Authorization a WHERE a.id.userid = :id_userid ORDER BY a.id.userid, a.id.nodeid"),
@NamedQuery(name = "getAuthorization", query = "SELECT a FROM Authorization a")})
public class Authorization implements Serializable {
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	private AuthorizationPK id;

    public Authorization() {
    }

	public AuthorizationPK getId() {
		return this.id;
	}

	public void setId(AuthorizationPK id) {
		this.id = id;
	}
	
}