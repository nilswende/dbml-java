package com.wn.dbml.model;

import java.util.*;

/**
 * The top-level representation of a DBML file.
 */
public class Database {
	private final Map<Schema, Schema> schemas = new LinkedHashMap<>();
	private final Set<Relationship> relationships = new LinkedHashSet<>();
	private Project project;
	
	public Schema getOrCreateSchema(final String name) {
		var schema = new Schema(name);
		schemas.putIfAbsent(schema, schema);
		return schemas.get(schema);
	}
	
	public Schema getSchema(final String name) {
		return schemas.get(new Schema(name));
	}
	
	public Set<Schema> getSchemas() {
		return Collections.unmodifiableSet(schemas.keySet());
	}
	
	public boolean containsAlias(final String alias) {
		return getAlias(alias) != null;
	}
	
	public Table getAlias(final String alias) {
		return getSchemas().stream()
				.flatMap(s -> s.getTables().stream())
				.filter(t -> alias.equals(t.getAlias()))
				.findAny()
				.orElse(null);
	}
	
	public Relationship createRelationship(final String name, final Relation relation, final List<Column> from, final List<Column> to, Map<RelationshipSetting, String> settings) {
		var relationship = new Relationship(name, relation, from, to, settings);
		var added = relationships.add(relationship);
		return added ? relationship : null;
	}
	
	public boolean containsRelationship(final String name) {
		return getRelationship(name) != null;
	}
	
	public Relationship getRelationship(final String name) {
		return relationships.stream().filter(c -> c.getName().equals(name)).findAny().orElse(null);
	}
	
	public Set<Relationship> getRelationships() {
		return Collections.unmodifiableSet(relationships);
	}
	
	public Project getProject() {
		return project;
	}
	
	public void setProject(final Project project) {
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
