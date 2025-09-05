package com.wn.dbml.model;

import com.wn.dbml.visitor.DatabaseElement;
import com.wn.dbml.visitor.DatabaseVisitor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Relationship implements SettingHolder<RelationshipSetting>, DatabaseElement {
	private final String name;
	private final Relation relation;
	private final List<Column> from, to;
	private final Map<RelationshipSetting, String> settings;
	
	Relationship(String name, Relation relation, List<Column> from, List<Column> to, Map<RelationshipSetting, String> settings) {
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
	public void addSetting(RelationshipSetting setting, String value) {
		settings.put(setting, value);
	}
	
	public Map<RelationshipSetting, String> getSettings() {
		return Collections.unmodifiableMap(settings);
	}
	
	@Override
	public boolean equals(Object o) {
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
	
	@Override
	public void accept(DatabaseVisitor visitor) {
		visitor.visit(this);
	}
}
