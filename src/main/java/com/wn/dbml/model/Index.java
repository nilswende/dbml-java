package com.wn.dbml.model;

import com.wn.dbml.CollectionUtil;
import com.wn.dbml.visitor.DatabaseElement;
import com.wn.dbml.visitor.DatabaseVisitor;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class Index implements SettingHolder<IndexSetting>, DatabaseElement {
	private final Table table;
	private final List<String> columns;
	private final Map<IndexSetting, String> settings = new EnumMap<>(IndexSetting.class);
	private Note note;
	
	Index(Table table, List<String> columns) {
		this.table = Objects.requireNonNull(table);
		this.columns = Objects.requireNonNull(columns);
		var duplicate = CollectionUtil.firstDuplicate(columns);
		if (duplicate != null) {
			throw new IllegalArgumentException("Column '%s' is already defined".formatted(duplicate));
		}
	}
	
	public Table getTable() {
		return table;
	}
	
	public List<String> getColumns() {
		return Collections.unmodifiableList(columns);
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
		var string = columns.stream()
				.map(c -> table.containsColumn(c) ? c : '`' + c + '`')
				.collect(Collectors.joining(", "));
		return columns.size() > 1 ? '(' + string + ')' : string;
	}
	
	@Override
	public void accept(DatabaseVisitor visitor) {
		visitor.visit(this);
	}
}
