package com.wn.dbml.model;

public enum RelationshipSetting implements Setting {
	DELETE,
	UPDATE,
	COLOR,
	;
	
	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
