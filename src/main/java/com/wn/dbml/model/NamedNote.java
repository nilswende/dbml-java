package com.wn.dbml.model;

import com.wn.dbml.util.Name;
import com.wn.dbml.visitor.DatabaseElement;
import com.wn.dbml.visitor.DatabaseVisitor;

import java.util.Objects;

public class NamedNote implements DatabaseElement {
	private final String name;
	private String value;
	
	public NamedNote(String name) {
		this.name = Name.requireNonEmpty(name);
	}
	
	public String getName() {
		return name;
	}
	
	public void setValue(String value) {
		this.value = Objects.requireNonNull(value);
	}
	
	public String getValue() {
		return value;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NamedNote that = (NamedNote) o;
		return Objects.equals(name, that.name);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name);
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
