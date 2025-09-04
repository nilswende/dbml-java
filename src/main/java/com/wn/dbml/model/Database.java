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
	private static final String EMPTY = "";
	private final Map<String, Schema> schemas = new LinkedHashMap<>();
	private final Set<Relationship> relationships = new LinkedHashSet<>();
	private final Map<String, NamedNote> namedNotes = new LinkedHashMap<>();
	private final Map<String, TableGroup> tableGroups = new LinkedHashMap<>();
	private final Map<String, Table> tablePartials = new LinkedHashMap<>();
	private final Schema tablePartialsSchema = new Schema(EMPTY);
	private Project project;
	
	public Schema getOrCreateSchema(String name) {
		if (name.isEmpty()) {
			throw new IllegalArgumentException("Schema must have a name");
		}
		var created = new Schema(name);
		var existing = schemas.putIfAbsent(name, created);
		return existing == null ? created : existing;
	}
	
	public Schema getSchema(String name) {
		return schemas.get(name);
	}
	
	public Set<Schema> getSchemas() {
		return Collections.unmodifiableSet(new LinkedHashSet<>(schemas.values()));
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
		return relationships.stream().filter(r -> r.getName().equals(name)).findAny().orElse(null);
	}
	
	public Set<Relationship> getRelationships() {
		return Collections.unmodifiableSet(relationships);
	}
	
	public NamedNote addNamedNote(String name) {
		if (name.isEmpty()) {
			throw new IllegalArgumentException("NamedNote must have a name");
		}
		var namedNote = new NamedNote(name);
		var added = namedNotes.putIfAbsent(name, namedNote) == null;
		return added ? namedNote : null;
	}
	
	public NamedNote getNamedNote(String name) {
		return namedNotes.get(name);
	}
	
	public Set<NamedNote> getNamedNotes() {
		return Collections.unmodifiableSet(new LinkedHashSet<>(namedNotes.values()));
	}
	
	public boolean containsTableGroup(String tableGroupName) {
		return getTableGroup(tableGroupName) != null;
	}
	
	public TableGroup getTableGroup(String tableGroupName) {
		return tableGroups.get(tableGroupName);
	}
	
	public TableGroup createTableGroup(String name) {
		var tableGroup = new TableGroup(name);
		var added = tableGroups.putIfAbsent(name, tableGroup) == null;
		return added ? tableGroup : null;
	}
	
	public Set<TableGroup> getTableGroups() {
		return Collections.unmodifiableSet(new LinkedHashSet<>(tableGroups.values()));
	}
	
	public boolean containsTablePartial(String tableName) {
		return getTablePartial(tableName) != null;
	}
	
	public Table getTablePartial(String tableName) {
		return tablePartials.get(tableName);
	}
	
	public Table createTablePartial(String name) {
		if (name.isEmpty()) {
			throw new IllegalArgumentException("TablePartial must have a name");
		}
		var table = new Table(tablePartialsSchema, name);
		var added = tablePartials.putIfAbsent(name, table) == null;
		return added ? table : null;
	}
	
	public Set<Table> getTablePartials() {
		return Collections.unmodifiableSet(new LinkedHashSet<>(tablePartials.values()));
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
				", namedNotes=" + namedNotes +
				", project=" + project +
				'}';
	}
}
