package com.dmcustoms.app.data.types;

public enum CardStatus {
	
	ACTIVE("Active"),
	BLOCKED("Blocked"),
	EXPIRED("Expired");
	
	public String status;
	
	private CardStatus(String status) {
		this.status = status;
	}

}
