package com.wn.dbml.model;

import com.wn.dbml.util.Name;
import com.wn.dbml.visitor.DatabaseElement;
import com.wn.dbml.visitor.DatabaseVisitor;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class Column implements SettingHolder<ColumnSetting>, DatabaseElement {
	private final Table table;
	private final String name, type;
	private final Map<ColumnSetting, String> settings = new EnumMap<>(ColumnSetting.class);
	private Note note;
	
	Column(Table table, String name, String type) {
		this.table = Objects.requireNonNull(table);
		this.name = Name.requireNonEmpty(name);
		this.type = Name.requireNonEmpty(type);
	}
	
	Column to(Table other) {
		var column = new Column(other, name, type);
		column.settings.putAll(this.settings);
		column.note = this.note;
		return column;
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
	public void addSetting(ColumnSetting setting, String value) {
		settings.put(setting, value);
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
		final Column column = (Column) o;
		return table.equals(column.table) && name.equals(column.name);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(table, name);
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
