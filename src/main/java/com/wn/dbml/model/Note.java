package com.wn.dbml.model;

public class Note {
	private final String value;
	
	public Note(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return value;
	}
}
