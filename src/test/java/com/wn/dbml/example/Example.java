package com.wn.dbml.example;

import com.wn.dbml.compiler.lexer.LexerImpl;
import com.wn.dbml.compiler.parser.ParserImpl;
import com.wn.dbml.model.Database;

public class Example {
	public static void main(String[] args) {
		var dbml = """
				Table table1 {
				  table integer
				}""";
		// parse the dbml
		Database database = new ParserImpl().parse(new LexerImpl(dbml));
		// process the database structure
		database.getSchemas().stream()
				.flatMap(schema -> schema.getTables().stream())
				.forEach(System.out::println); // prints "table1"
	}
}
