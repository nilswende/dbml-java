package com.wn.dbml.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Index implements SettingHolder<IndexSetting> {
	private final Table table;
	private final Map<String, Column> columns = new LinkedHashMap<>();
	private final Map<IndexSetting, String> settings = new EnumMap<>(IndexSetting.class);
	private Note note;
	
	Index(Table table) {
		this.table = Objects.requireNonNull(table);
	}
	
	public Table getTable() {
		return table;
	}
	
	public boolean addColumn(String column) {
		var containsColumn = columns.containsKey(column);
		if (!containsColumn) {
			columns.put(column, table.getColumn(column));
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
	public String toString() {
		return columns.entrySet()
				.stream()
				.map(e -> e.getValue() == null ? '`' + e.getKey() + '`' : e.getKey())
				.collect(Collectors.joining(", ", "(", ")"));
	}
}
