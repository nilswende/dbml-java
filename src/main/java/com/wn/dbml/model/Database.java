package com.wn.dbml.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The top-level representation of a DBML file.
 */
public class Database {
	private final Map<Schema, Schema> schemas = new LinkedHashMap<>();
	private final Set<Relationship> relationships = new LinkedHashSet<>();
	private Project project;
	
	public Schema getOrCreateSchema(String name) {
		var schema = new Schema(name);
		schemas.putIfAbsent(schema, schema);
		return schemas.get(schema);
	}
	
	public Schema getSchema(String name) {
		return schemas.get(new Schema(name));
	}
	
	public Set<Schema> getSchemas() {
		return Collections.unmodifiableSet(schemas.keySet());
	}
	
	public boolean containsAlias(String alias) {
		return getAlias(alias) != null;
	}
	
	public Table getAlias(String alias) {
		return getSchemas().stream()
				.flatMap(s -> s.getTables().stream())
				.filter(t -> alias.equals(t.getAlias()))
				.findAny()
				.orElse(null);
	}
	
	public Relationship createRelationship(String name, Relation relation, List<Column> from, List<Column> to, Map<RelationshipSetting, String> settings) {
		var relationship = new Relationship(name, relation, from, to, settings);
		var added = relationships.add(relationship);
		return added ? relationship : null;
	}
	
	public boolean containsRelationship(String name) {
		return getRelationship(name) != null;
	}
	
	public Relationship getRelationship(String name) {
		return relationships.stream().filter(c -> c.getName().equals(name)).findAny().orElse(null);
	}
	
	public Set<Relationship> getRelationships() {
		return Collections.unmodifiableSet(relationships);
	}
	
	public Project getProject() {
		return project;
	}
	
	public void setProject(Project project) {
		this.project = project;
	}
	
	@Override
	public String toString() {
		return "Database{" +
			   "schemas=" + schemas +
			   ", relationships=" + relationships +
			   ", project=" + project +
			   '}';
	}
}
