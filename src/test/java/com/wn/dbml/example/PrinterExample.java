package com.wn.dbml.example;

import com.wn.dbml.compiler.DbmlParser;
import com.wn.dbml.model.Database;
import com.wn.dbml.printer.DbmlPrinter;

public class PrinterExample {
	public static void main(String[] args) {
		var dbml = """
				Table table1 {
				  id integer
				}""";
		// parse the dbml
		Database database = DbmlParser.parse(dbml);
		// print the database structure
		var printer = new DbmlPrinter();
		database.accept(printer);
		System.out.println(printer); // prints the above dbml
	}
}
