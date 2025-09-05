package com.wn.dbml.model;

import com.wn.dbml.visitor.DatabaseElement;
import com.wn.dbml.visitor.DatabaseVisitor;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Index implements SettingHolder<IndexSetting>, DatabaseElement {
	private final Table table;
	private final Map<String, Column> columns = new LinkedHashMap<>();
	private final Map<IndexSetting, String> settings = new EnumMap<>(IndexSetting.class);
	private Note note;
	
	Index(Table table) {
		this.table = Objects.requireNonNull(table);
	}
	
	Index to(Table other) {
		var index = new Index(other);
		this.columns.keySet().forEach(index::addColumn);
		index.settings.putAll(this.settings);
		index.note = this.note;
		return index;
	}
	
	public Table getTable() {
		return table;
	}
	
	public boolean addColumn(String columnName) {
		var containsColumn = columns.containsKey(columnName);
		if (!containsColumn) {
			columns.put(columnName, table.getColumn(columnName));
		}
		return !containsColumn;
	}
	
	public Map<String, Column> getColumns() {
		return Collections.unmodifiableMap(columns);
	}
	
	@Override
	public void addSetting(IndexSetting setting, String value) {
		settings.put(setting, value);
	}
	
	public Map<IndexSetting, String> getSettings() {
		return Collections.unmodifiableMap(settings);
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
		Index index = (Index) o;
		return Objects.equals(table, index.table) && Objects.equals(columns, index.columns);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(table, columns);
	}
	
	@Override
	public String toString() {
		var string = columns.entrySet()
				.stream()
				.map(e -> e.getValue() == null ? '`' + e.getKey() + '`' : e.getKey())
				.collect(Collectors.joining(", "));
		return columns.size() > 1 ? '(' + string + ')' : string;
	}
	
	@Override
	public void accept(DatabaseVisitor visitor) {
		visitor.visit(this);
	}
}
