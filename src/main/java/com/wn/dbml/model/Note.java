package com.wn.dbml.model;

import java.util.Objects;

public class Note {
	private final String value;
	
	public Note(String value) {
		this.value = Objects.requireNonNull(value);
	}
	
	public String getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return value;
	}
}
