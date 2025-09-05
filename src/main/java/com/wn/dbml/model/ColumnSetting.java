package com.wn.dbml.model;

public enum ColumnSetting implements Setting {
	PRIMARY_KEY,
	NOT_NULL,
	UNIQUE,
	INCREMENT,
	DEFAULT,
	;
	
	@Override
	public String toString() {
		return name().toLowerCase().replace('_', ' ');
	}
}
