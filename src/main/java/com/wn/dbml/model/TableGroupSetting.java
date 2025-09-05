package com.wn.dbml.model;

public enum TableGroupSetting implements Setting {
	COLOR,
	;
	
	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
