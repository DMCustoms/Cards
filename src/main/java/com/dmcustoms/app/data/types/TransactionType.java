package com.dmcustoms.app.data.types;

public enum TransactionType {

	WRITEOFF("Write-off of funds"),
	TRANSFER("Funds transfer");
	
	public String type;
	
	private TransactionType(String type) {
		this.type = type;
	}
	
}
