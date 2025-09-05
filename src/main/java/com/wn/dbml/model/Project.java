package com.wn.dbml.model;

import com.wn.dbml.Name;
import com.wn.dbml.visitor.DatabaseElement;
import com.wn.dbml.visitor.DatabaseVisitor;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class Project implements DatabaseElement {
	private final String name;
	private final Map<String, String> properties = new LinkedHashMap<>();
	private Note note;
	
	public Project(String name) {
		this.name = Objects.requireNonNull(name);
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
	
	public void setNote(Note note) {
		this.note = note;
	}
	
	@Override
	public String toString() {
		return Name.of(name);
	}
	
	@Override
	public void accept(DatabaseVisitor visitor) {
		visitor.visit(this);
	}
}
