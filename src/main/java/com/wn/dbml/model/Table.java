package com.wn.dbml.model;

import java.util.*;

public class Table implements SettingHolder<TableSetting> {
	private final Schema schema;
	private final String name;
	private final Map<TableSetting, String> settings = new EnumMap<>(TableSetting.class);
	private final Set<Column> columns = new LinkedHashSet<>();
	private final Set<Index> indexes = new LinkedHashSet<>();
	private String alias;
	private Note note;
	
	Table(final Schema schema, final String name) {
		this.schema = Objects.requireNonNull(schema);
		this.name = Objects.requireNonNull(name);
	}
	
	public Schema getSchema() {
		return schema;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public void addSetting(final TableSetting setting, final String value) {
		settings.put(setting, value);
	}
	
	public Map<TableSetting, String> getSettings() {
		return Collections.unmodifiableMap(settings);
	}
	
	public boolean containsColumn(final String columnName) {
		return getColumn(columnName) != null;
	}
	
	public Column getColumn(final String columnName) {
		return columns.stream().filter(c -> c.getName().equals(columnName)).findAny().orElse(null);
	}
	
	public Column addColumn(final String columnName, final String datatype) {
		var column = new Column(this, columnName, datatype);
		var added = columns.add(column);
		return added ? column : null;
	}
	
	public Set<Column> getColumns() {
		return Collections.unmodifiableSet(columns);
	}
	
	public Index addIndex() {
		var index = new Index(this);
		var added = indexes.add(index);
		return added ? index : null;
	}
	
	public Set<Index> getIndexes() {
		return Collections.unmodifiableSet(indexes);
	}
	
	public String getAlias() {
		return alias;
	}
	
	public void setAlias(final String alias) {
		this.alias = alias;
	}
	
	public Note getNote() {
		return note;
	}
	
	public void setNote(final Note note) {
		this.note = note;
	}
	
	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final Table table = (Table) o;
		return Objects.equals(schema, table.schema) && name.equals(table.name);
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
