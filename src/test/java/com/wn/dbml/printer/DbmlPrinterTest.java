package com.wn.dbml.printer;

import com.wn.dbml.compiler.lexer.LexerImpl;
import com.wn.dbml.compiler.parser.ParserImpl;
import com.wn.dbml.model.Database;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DbmlPrinterTest {
	private Database parse(String dbml) {
		return new ParserImpl().parse(new LexerImpl(dbml));
	}
	
	@Test
	void printProject() {
		var dbml = """
				Project project_name {
				  database_type: 'PostgreSQL'
				
				  Note: 'Description of the project'
				}""";
		var database = parse(dbml);
		
		var printer = new DbmlPrinter();
		database.accept(printer);
		
		assertEquals(dbml, printer.toString());
	}
	
	@Test
	void printProjectQuoted() {
		var dbml = """
				Project "project-name" {
				  database_type: 'PostgreSQL'
				
				  Note: '''Description of
				           the project'''
				}""";
		var database = parse(dbml);
		
		var printer = new DbmlPrinter();
		database.accept(printer);
		
		assertEquals(dbml, printer.toString());
	}
	
	@Test
	void printTable() {
		var dbml = """
				Table s.users as U [headercolor: #3498DB] {
				  id integer [primary key, increment, note: 'replace text here']
				  username varchar(255) [not null, unique, default: null]
				  weight "bigint unsigned" [default: 1.23]
				
				  Note: 'Stores user data'
				}""";
		var database = parse(dbml);
		
		var printer = new DbmlPrinter();
		database.accept(printer);
		
		assertEquals(dbml, printer.toString());
	}
	
	@Test
	void printIndex() {
		var dbml = """
				Table bookings {
				  id integer
				  country varchar
				  booking_date date
				  created_at timestamp
				
				  indexes {
				    (id, country) [pk]
				    created_at [name: 'created_at_index', note: 'Date']
				    booking_date
				    (country, booking_date) [unique]
				    booking_date [type: hash]
				    `id*2`
				    (`id*3`, `getdate()`)
				    (`id*3`, id)
				  }
				}""";
		var database = parse(dbml);
		
		var printer = new DbmlPrinter();
		database.accept(printer);
		
		assertEquals(dbml, printer.toString());
	}
}