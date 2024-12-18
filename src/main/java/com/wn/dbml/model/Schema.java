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
	
	Schema(String name) {
		this.name = Objects.requireNonNull(name);
	}
	
	public String getName() {
		return name;
	}
	
	public boolean containsTable(String tableName) {
		return getTable(tableName) != null;
	}
	
	public Table getTable(String tableName) {
		return tables.stream().filter(c -> c.getName().equals(tableName)).findAny().orElse(null);
	}
	
	public Table createTable(String name) {
		var table = new Table(this, name);
		var added = tables.add(table);
		return added ? table : null;
	}
	
	public Set<Table> getTables() {
		return Collections.unmodifiableSet(tables);
	}
	
	public boolean containsEnum(String enumName) {
		return getEnum(enumName) != null;
	}
	
	public Enum getEnum(String enumName) {
		return enums.stream().filter(c -> c.getName().equals(enumName)).findAny().orElse(null);
	}
	
	public Enum createEnum(String name) {
		var anEnum = new Enum(this, name);
		var added = enums.add(anEnum);
		return added ? anEnum : null;
	}
	
	public Set<Enum> getEnums() {
		return Collections.unmodifiableSet(enums);
	}
	
	public boolean containsTableGroup(String tableGroupName) {
		return getTableGroup(tableGroupName) != null;
	}
	
	public TableGroup getTableGroup(String tableGroupName) {
		return tableGroups.stream().filter(c -> c.getName().equals(tableGroupName)).findAny().orElse(null);
	}
	
	public TableGroup createTableGroup(String name) {
		var tableGroup = new TableGroup(this, name);
		var added = tableGroups.add(tableGroup);
		return added ? tableGroup : null;
	}
	
	public Set<TableGroup> getTableGroups() {
		return Collections.unmodifiableSet(tableGroups);
	}
	
	@Override
	public boolean equals(Object o) {
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
