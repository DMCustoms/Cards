package com.dmcustoms.app.data.types;

import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;

public enum Authorities implements GrantedAuthority {
	
	USER("ROLE_USER"),
	ADMIN("ROLE_ADMIN");
	
	public String authority;
	
	private Authorities(String authority) {
		this.authority = authority;
	}
	
	@Override
	public @Nullable String getAuthority() {
		return this.authority;
	}
	
}
