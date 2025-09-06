package com.wn.dbml;

import com.wn.dbml.model.Enum;
import com.wn.dbml.model.Schema;
import com.wn.dbml.model.Table;

import java.util.List;

/**
 * Names of elements.
 */
public final class Name {
	private Name() {
	}
	
	private static String quote(String s) {
		return !s.isBlank() && Chars.isWordChars(s) ? s : '"' + s + '"';
	}
	
	public static String of(String name) {
		return quote(name);
	}
	
	public static String of(Schema schema, String table) {
		return ofTable(schema.toString(), table);
	}
	
	public static String of(Table table, String column) {
		return table + "." + of(column);
	}
	
	public static String of(Enum anEnum, String value) {
		return anEnum + "." + of(value);
	}
	
	public static String ofTable(String schema, String table) {
		return schema.isEmpty() || schema.equals(Schema.DEFAULT_NAME) ? of(table) : of(schema) + '.' + of(table);
	}
	
	public static String ofColumn(String schema, String table, String column) {
		return ofTable(schema, table) + '.' + of(column);
	}
	
	public static String ofColumns(String schema, String table, List<String> columns) {
		return columns.size() < 2 ? ofColumn(schema, table, columns.getFirst()) : ofTable(schema, table) + ".(" + String.join(", ", columns) + ')';
	}
	
	public static String nullIfEmpty(String name) {
		return name != null && name.isEmpty() ? null : name;
	}
	
	public static String requireNonEmpty(String name) {
		if (name == null || name.isEmpty()) {
			throw new IllegalArgumentException("Name must not be empty");
		}
		return name;
	}
}
