package com.wn.dbml.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Project {
	private final String name;
	private final Map<String, String> properties = new LinkedHashMap<>();
	private Note note;
	
	public Project(final String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Map<String, String> getProperties() {
		return properties;
	}
	
	public Note getNote() {
		return note;
	}
	
	public void setNote(final Note note) {
		this.note = note;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
