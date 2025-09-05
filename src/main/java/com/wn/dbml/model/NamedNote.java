package com.wn.dbml.model;

import com.wn.dbml.Name;
import com.wn.dbml.visitor.DatabaseElement;
import com.wn.dbml.visitor.DatabaseVisitor;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

public class NamedNote implements SettingHolder<NamedNoteSetting>, DatabaseElement {
	private final String name;
	private final Map<NamedNoteSetting, String> settings = new EnumMap<>(NamedNoteSetting.class);
	private String value;
	
	public NamedNote(String name) {
		this.name = Objects.requireNonNull(name);
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public void addSetting(NamedNoteSetting setting, String value) {
		settings.put(setting, value);
	}
	
	public Map<NamedNoteSetting, String> getSettings() {
		return Collections.unmodifiableMap(settings);
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		NamedNote that = (NamedNote) o;
		return Objects.equals(name, that.name);
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
