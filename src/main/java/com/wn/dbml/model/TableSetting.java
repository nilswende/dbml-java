package com.wn.dbml.model;

public enum TableSetting implements Setting {
	HEADERCOLOR,
	;
	
	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
