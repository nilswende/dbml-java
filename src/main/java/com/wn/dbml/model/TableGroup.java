package com.wn.dbml.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class TableGroup {
	private final Schema schema;
	private final String name;
	private final Set<Table> tables = new LinkedHashSet<>();
	private Note note;
	
	TableGroup(final Schema schema, final String name) {
		this.schema = Objects.requireNonNull(schema);
		this.name = Objects.requireNonNull(name);
	}
	
	public Schema getSchema() {
		return schema;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean addTable(final Table table) {
		return tables.add(table);
	}
	
	public Set<Table> getTables() {
		return Collections.unmodifiableSet(tables);
	}
	
	public Note getNote() {
		return note;
	}
	
	public void setNote(Note note) {
		this.note = note;
	}
	
	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final TableGroup that = (TableGroup) o;
		return schema.equals(that.schema) && name.equals(that.name);
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
