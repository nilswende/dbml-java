package com.wn.dbml.printer;

import com.wn.dbml.model.Database;
import com.wn.dbml.model.Schema;
import com.wn.dbml.model.Table;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * A printable DBML database.
 */
public class PrintableDatabase extends Database {
	private final Schema tablePartialsSchema;
	private final Map<Table, Set<String>> tablePartialRefs;
	
	public PrintableDatabase() {
		tablePartialsSchema = new Database().getOrCreateSchema(Schema.DEFAULT_NAME);
		tablePartialRefs = new HashMap<>();
	}
	
	public Schema getTablePartialsSchema() {
		return tablePartialsSchema;
	}
	
	public boolean addTablePartialRef(Table table, String ref) {
		return tablePartialRefs.computeIfAbsent(table, x -> new LinkedHashSet<>()).add(ref);
	}
}
