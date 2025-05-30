package com.wn.dbml.model;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class TableGroup implements SettingHolder<TableGroupSetting> {
	private final Schema schema;
	private final String name;
	private final Map<TableGroupSetting, String> settings = new EnumMap<>(TableGroupSetting.class);
	private final Set<Table> tables = new LinkedHashSet<>();
	private Note note;
	
	TableGroup(Schema schema, String name) {
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
	public void addSetting(TableGroupSetting setting, String value) {
		settings.put(setting, value);
	}
	
	public Map<TableGroupSetting, String> getSettings() {
		return Collections.unmodifiableMap(settings);
	}
	
	public boolean addTable(Table table) {
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
	public boolean equals(Object o) {
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
