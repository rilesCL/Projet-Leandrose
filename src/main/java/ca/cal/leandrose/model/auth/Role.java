package ca.cal.leandrose.model.auth;

import java.util.HashSet;
import java.util.Set;

public enum Role{
	GESTIONNAIRE("ROLE_GESTIONNAIRE"),
	PREPOSE("ROLE_PREPOSE"),
	EMPLOYEUR("ROLE_EMPLOYEUR"),
	;

	private final String string;
	private final Set<Role> managedRoles = new HashSet<>();

	static{
		GESTIONNAIRE.managedRoles.add(PREPOSE);
		GESTIONNAIRE.managedRoles.add(EMPLOYEUR);
	}

	Role(String string){
		this.string = string;
	}

	@Override
	public String toString(){
		return string;
	}

}
