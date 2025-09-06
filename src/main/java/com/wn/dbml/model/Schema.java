package com.wn.dbml.model;

import com.wn.dbml.util.Name;
import com.wn.dbml.visitor.DatabaseElement;
import com.wn.dbml.visitor.DatabaseVisitor;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Schema implements DatabaseElement {
	public static final String DEFAULT_NAME = "public";
	private final Database database;
	private final String name;
	private final Map<String, Table> tables = new LinkedHashMap<>();
	private final Map<String, Enum> enums = new LinkedHashMap<>();
	
	Schema(Database database, String name) {
		this.database = database;
		this.name = Objects.requireNonNull(name);
	}
	
	public Database getDatabase() {
		return database;
	}
	
	public String getName() {
		return name;
	}
	
	public boolean containsTable(String tableName) {
		return getTable(tableName) != null;
	}
	
	public Table getTable(String tableName) {
		return tables.get(tableName);
	}
	
	public Table createTable(String name) {
		if (name.isEmpty()) {
			throw new IllegalArgumentException("Table must have a name");
		}
		var table = new Table(this, name);
		var added = tables.putIfAbsent(name, table) == null;
		if (!added) {
			throw new IllegalArgumentException("Table '%s' is already defined".formatted(name));
		}
		return table;
	}
	
	public Set<Table> getTables() {
		return Collections.unmodifiableSet(new LinkedHashSet<>(tables.values()));
	}
	
	public boolean containsEnum(String enumName) {
		return getEnum(enumName) != null;
	}
	
	public Enum getEnum(String enumName) {
		return enums.get(enumName);
	}
	
	public Enum createEnum(String name) {
		var anEnum = new Enum(this, name);
		var added = enums.putIfAbsent(name, anEnum) == null;
		return added ? anEnum : null;
	}
	
	public Set<Enum> getEnums() {
		return Collections.unmodifiableSet(new LinkedHashSet<>(enums.values()));
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
		return Name.of(name);
	}
	
	@Override
	public void accept(DatabaseVisitor visitor) {
		visitor.visit(this);
	}
}
