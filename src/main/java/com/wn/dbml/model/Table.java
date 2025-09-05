package com.wn.dbml.model;

import com.wn.dbml.Name;
import com.wn.dbml.visitor.DatabaseElement;
import com.wn.dbml.visitor.DatabaseVisitor;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Table implements SettingHolder<TableSetting>, DatabaseElement {
	private final Schema schema;
	private final String name;
	private final Map<TableSetting, String> settings = new EnumMap<>(TableSetting.class);
	private final Map<String, Column> columns = new LinkedHashMap<>();
	private final Set<Index> indexes = new LinkedHashSet<>();
	private Alias alias;
	private Note note;
	
	Table(Schema schema, String name) {
		this.schema = Objects.requireNonNull(schema);
		this.name = Objects.requireNonNull(name);
	}
	
	public void inject(Table other) {
		Objects.requireNonNull(other);
		// settings
		for (var entry : other.settings.entrySet()) {
			var setting = entry.getKey();
			this.settings.putIfAbsent(setting, entry.getValue());
		}
		// columns
		for (var column : other.columns.values()) {
			this.columns.putIfAbsent(column.getName(), column.to(this));
		}
		// indexes
		for (var index : other.indexes) {
			this.indexes.add(index.to(this));
		}
		// note
		if (this.note == null && other.note != null) {
			this.note = other.note;
		}
	}
	
	public Schema getSchema() {
		return schema;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public void addSetting(TableSetting setting, String value) {
		settings.put(setting, value);
	}
	
	public Map<TableSetting, String> getSettings() {
		return Collections.unmodifiableMap(settings);
	}
	
	public boolean containsColumn(String columnName) {
		return getColumn(columnName) != null;
	}
	
	public Column getColumn(String columnName) {
		return columns.get(columnName);
	}
	
	public Column addColumn(String columnName, String datatype) {
		if (columnName.isEmpty()) {
			throw new IllegalArgumentException("Column must have a name");
		}
		if (datatype.isEmpty()) {
			throw new IllegalArgumentException("Invalid column type");
		}
		var column = new Column(this, columnName, datatype);
		var added = columns.putIfAbsent(columnName, column) == null;
		return added ? column : null;
	}
	
	public Set<Column> getColumns() {
		return Collections.unmodifiableSet(new LinkedHashSet<>(columns.values()));
	}
	
	public Index getIndex(String indexName) {
		return indexName == null ? null
				: indexes.stream().filter(i -> indexName.equals(i.getSettings().get(IndexSetting.NAME))).findAny().orElse(null);
	}
	
	public Index addIndex() {
		var index = new Index(this);
		var added = indexes.add(index);
		return added ? index : null;
	}
	
	public Set<Index> getIndexes() {
		return Collections.unmodifiableSet(indexes);
	}
	
	public Alias getAlias() {
		return alias;
	}
	
	public void setAlias(Alias alias) {
		this.alias = alias;
	}
	
	public Note getNote() {
		return note;
	}
	
	public void setNote(Note note) {
		this.note = note;
	}
	
	@Override
	public boolean equals(Object o) {
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
	
	@Override
	public void accept(DatabaseVisitor visitor) {
		visitor.visit(this);
	}
}
