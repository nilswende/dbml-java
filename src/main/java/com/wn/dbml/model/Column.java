package com.wn.dbml.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class Column implements SettingHolder<ColumnSetting> {
	private final Table table;
	private final String name, type;
	private final Map<ColumnSetting, String> settings = new EnumMap<>(ColumnSetting.class);
	private Note note;
	
	Column(final Table table, final String name, final String type) {
		this.table = Objects.requireNonNull(table);
		this.name = Objects.requireNonNull(name);
		this.type = Objects.requireNonNull(type);
	}
	
	public Table getTable() {
		return table;
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public Map<ColumnSetting, String> getSettings() {
		return Collections.unmodifiableMap(settings);
	}
	
	@Override
	public void addSetting(final ColumnSetting setting, final String value) {
		settings.put(setting, value);
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
		final Column column = (Column) o;
		return table.equals(column.table) && name.equals(column.name);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(table, name);
	}
	
	@Override
	public String toString() {
		return Name.of(table, name);
	}
}
