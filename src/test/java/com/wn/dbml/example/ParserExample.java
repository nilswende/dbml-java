package com.wn.dbml.example;

import com.wn.dbml.compiler.DbmlParser;
import com.wn.dbml.model.Database;

public class ParserExample {
	public static void main(String[] args) {
		var dbml = """
				Table table1 {
				  id integer
				}""";
		// parse the dbml
		Database database = DbmlParser.parse(dbml);
		// process the database structure
		database.getSchemas().stream()
				.flatMap(schema -> schema.getTables().stream())
				.forEach(System.out::println); // prints "table1"
	}
}
