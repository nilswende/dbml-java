package com.wn.dbml.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public class Schema {
	public static final String DEFAULT_NAME = "public";
	private final String name;
	private final Set<Table> tables = new LinkedHashSet<>();
	private final Set<Enum> enums = new LinkedHashSet<>();
	private final Set<TableGroup> tableGroups = new LinkedHashSet<>();
	
	Schema(final String name) {
		this.name = Objects.requireNonNull(name);
	}
	
	public String getName() {
		return name;
	}
	
	public boolean containsTable(final String tableName) {
		return getTable(tableName) != null;
	}
	
	public Table getTable(final String tableName) {
		return tables.stream().filter(c -> c.getName().equals(tableName)).findAny().orElse(null);
	}
	
	public Table createTable(final String name) {
		var table = new Table(this, name);
		var added = tables.add(table);
		return added ? table : null;
	}
	
	public Set<Table> getTables() {
		return Collections.unmodifiableSet(tables);
	}
	
	public boolean containsEnum(final String enumName) {
		return getEnum(enumName) != null;
	}
	
	public Enum getEnum(final String enumName) {
		return enums.stream().filter(c -> c.getName().equals(enumName)).findAny().orElse(null);
	}
	
	public Enum createEnum(final String name) {
		var anEnum = new Enum(this, name);
		var added = enums.add(anEnum);
		return added ? anEnum : null;
	}
	
	public Set<Enum> getEnums() {
		return Collections.unmodifiableSet(enums);
	}
	
	public boolean containsTableGroup(final String tableGroupName) {
		return getTableGroup(tableGroupName) != null;
	}
	
	public TableGroup getTableGroup(final String tableGroupName) {
		return tableGroups.stream().filter(c -> c.getName().equals(tableGroupName)).findAny().orElse(null);
	}
	
	public TableGroup createTableGroup(final String name) {
		var tableGroup = new TableGroup(this, name);
		var added = tableGroups.add(tableGroup);
		return added ? tableGroup : null;
	}
	
	public Set<TableGroup> getTableGroups() {
		return Collections.unmodifiableSet(tableGroups);
	}
	
	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final Schema schema = (Schema) o;
		return name.equals(schema.name);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
	
	@Override
	public String toString() {
		return name;
	}
}
