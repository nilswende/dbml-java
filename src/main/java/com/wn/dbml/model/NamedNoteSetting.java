package com.wn.dbml.model;

public enum NamedNoteSetting implements Setting {
	HEADERCOLOR,
	;
	
	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
