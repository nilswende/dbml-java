package com.wn.dbml.model;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Relationship implements SettingHolder<RelationshipSetting> {
	private final String name;
	private final Relation relation;
	private final List<Column> from, to;
	private final Map<RelationshipSetting, String> settings;
	
	Relationship(final String name, final Relation relation, final List<Column> from, final List<Column> to, Map<RelationshipSetting, String> settings) {
		this.name = name;
		this.relation = Objects.requireNonNull(relation);
		this.from = Objects.requireNonNull(from);
		this.to = Objects.requireNonNull(to);
		this.settings = Objects.requireNonNull(settings);
	}
	
	public String getName() {
		return name;
	}
	
	public Relation getRelation() {
		return relation;
	}
	
	public List<Column> getFrom() {
		return from;
	}
	
	public List<Column> getTo() {
		return to;
	}
	
	@Override
	public void addSetting(final RelationshipSetting setting, final String value) {
		settings.put(setting, value);
	}
	
	public Map<RelationshipSetting, String> getSettings() {
		return Collections.unmodifiableMap(settings);
	}
	
	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final Relationship that = (Relationship) o;
		return from.equals(that.from) && to.equals(that.to);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(from, to);
	}
	
	@Override
	public String toString() {
		return from + " " + relation + " " + to;
	}
}
