package com.wn.dbml.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class Enum {
	private final Schema schema;
	private final String name;
	private final Set<EnumValue> values = new LinkedHashSet<>();
	
	Enum(Schema schema, String name) {
		this.schema = Objects.requireNonNull(schema);
		this.name = Objects.requireNonNull(name);
	}
	
	public Schema getSchema() {
		return schema;
	}
	
	public String getName() {
		return name;
	}
	
	public EnumValue addValue(String name) {
		if (name.isEmpty()) {
			throw new IllegalArgumentException("Enum value must have a name");
		}
		var value = new EnumValue(this, name);
		var added = values.add(value);
		return added ? value : null;
	}
	
	public Set<EnumValue> getValues() {
		return Collections.unmodifiableSet(values);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final Enum anEnum = (Enum) o;
		return schema.equals(anEnum.schema) && name.equals(anEnum.name);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(schema, name);
	}
	
	@Override
	public String toString() {
		return Name.of(schema, name);
	}
}
