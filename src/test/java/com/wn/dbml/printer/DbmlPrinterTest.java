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
	void testFormatter() {
		var dbml = """
				Table users {
				    id integer
				}""";
		var database = parse(dbml);
		
		var printer = new DbmlPrinter(new DbmlFormatter.Builder().setIndentation("    ").setLinebreak("\r\n").build());
		database.accept(printer);
		
		assertEquals(dbml, printer.toString().replace("\r\n", "\n"));
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
				  size "bigint unsigned" [default: 1.23]
				
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
	
	@Test
	void printRelationship() {
		var dbml = """
				Table table1 {
				  id integer
				  column1 integer
				}
				
				Table table2 {
				  id integer
				  column2 integer
				}
				
				Ref: table2.column2 - table1.column1""";
		var database = parse(dbml);
		
		var printer = new DbmlPrinter();
		database.accept(printer);
		
		assertEquals(dbml, printer.toString());
	}
	
	@Test
	void printRelationshipComposite() {
		var dbml = """
				Table schema1.table1 {
				  id integer
				  column1 integer
				}
				
				Table schema2.table2 {
				  id integer
				  column2 integer
				}
				
				Ref r1: schema2.table2.(id, column2) - schema1.table1.(id, column1)""";
		var database = parse(dbml);
		
		var printer = new DbmlPrinter();
		database.accept(printer);
		
		assertEquals(dbml, printer.toString());
	}
	
	@Test
	void printEnum() {
		var dbml = """
				enum job_status {
				  created [note: 'Waiting to be processed']
				  running
				  done
				  failure
				}
				
				Table jobs {
				  id integer
				  status job_status
				}""";
		var database = parse(dbml);
		
		var printer = new DbmlPrinter();
		database.accept(printer);
		
		assertEquals(dbml, printer.toString());
	}
	
	@Test
	void printNamedNote() {
		var dbml = """
				Table table1 {
				  table integer
				}
				
				Note single_line_note {
				  'This is a single line note'
				}
				
				Note multiple_lines_note {
				  '''This is a multiple lines note
				     This string can spans over multiple lines.'''
				}""";
		var database = parse(dbml);
		
		var printer = new DbmlPrinter();
		database.accept(printer);
		
		assertEquals(dbml, printer.toString());
	}
	
	@Test
	void printTableGroup() {
		var dbml = """
				Table table1 {
				  id integer
				}
				
				Table table2 {
				  id integer
				}
				
				Table table3 as C {
				  id integer
				}
				
				TableGroup tablegroup_name [color: #fff] {
				  table1
				  table2
				  C
				
				  Note: 'group note element'
				}""";
		var database = parse(dbml);
		
		var printer = new DbmlPrinter();
		database.accept(printer);
		
		assertEquals(dbml, printer.toString());
	}
	
	@Test
	void printTablePartial() {
		// 'now()' should be `now()`, but there's currently no way
		var dbml = """
				TablePartial base_template [headercolor: #ff0000] {
				  id int [primary key, not null]
				  created_at timestamp [default: 'now()']
				  updated_at timestamp [default: 'now()']
				
				  Note: 'base note'
				}
				
				TablePartial soft_delete_template {
				  delete_status boolean [not null]
				  deleted_at timestamp [default: 'now()']
				}
				
				TablePartial email_index {
				  email varchar [unique]
				
				  indexes {
				    email [name: 'U__email', unique]
				  }
				}
				
				Table users {
				  ~base_template
				  ~soft_delete_template
				  ~email_index
				  name varchar
				}""";
		var database = parse(dbml);
		
		var printer = new DbmlPrinter();
		database.accept(printer);
		
		assertEquals(dbml, printer.toString());
	}
}