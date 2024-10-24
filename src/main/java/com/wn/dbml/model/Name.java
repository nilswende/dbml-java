package com.wn.dbml.model;

import java.util.List;

/**
 * Names of elements.
 */
public final class Name {
	private Name() {
	}
	
	public static String of(Schema schema, String table) {
		return ofTable(schema.toString(), table);
	}
	
	public static String of(Table table, String column) {
		return table + "." + column;
	}
	
	public static String of(Enum anEnum, String value) {
		return anEnum + "." + value;
	}
	
	public static String ofTable(String schema, String table) {
		return schema.equals(Schema.DEFAULT_NAME) ? table : schema + '.' + table;
	}
	
	public static String ofColumn(String schema, String table, String column) {
		return ofTable(schema, table) + '.' + column;
	}
	
	public static String ofColumns(String schema, String table, List<String> columns) {
		return ofTable(schema, table) + ".(" + String.join(", ", columns) + ')';
	}
}
