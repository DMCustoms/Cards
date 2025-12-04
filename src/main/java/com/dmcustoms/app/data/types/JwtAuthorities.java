package com.dmcustoms.app.data.types;

public enum JwtAuthorities {

	JWT_REFRESH("JWT_REFRESH"),
	JWT_LOGOUT("JWT_LOGOUT"),
	JWT_ACCESS("JWT_ACCESS");
	
	public String authority;
	
	private JwtAuthorities(String authority) {
		this.authority = authority;
	}
	
}
