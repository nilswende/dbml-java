package com.wn.dbml.model;

public enum IndexSetting implements Setting {
	TYPE,
	NAME,
	UNIQUE,
	PK,
	;
	
	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
