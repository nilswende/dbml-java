package com.wn.dbml.model;

import java.util.List;

/**
 * Names of elements.
 */
public final class Name {
	private Name() {
	}
	
	public static String of(final Schema schema, final String table) {
		return ofTable(schema.toString(), table);
	}
	
	public static String of(final Table table, final String column) {
		return table + "." + column;
	}
	
	public static String of(final Enum anEnum, final String value) {
		return anEnum + "." + value;
	}
	
	public static String ofTable(final String schema, final String table) {
		return schema.equals(Schema.DEFAULT_NAME) ? table : schema + '.' + table;
	}
	
	public static String ofColumn(final String schema, final String table, final String column) {
		return ofTable(schema, table) + '.' + column;
	}
	
	public static String ofColumns(final String schema, final String table, final List<String> columns) {
		return ofTable(schema, table) + ".(" + String.join(", ", columns) + ')';
	}
}
