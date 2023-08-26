package com.wn.dbml.compiler.parser;

import com.wn.dbml.compiler.lexer.LexerImpl;
import com.wn.dbml.model.Database;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class ParserImplTest {
	
	private Database parse(final String dbml) {
		return new ParserImpl().parse(new LexerImpl(dbml));
	}
	
	@Test
	@Disabled
	void printDatabase() {
		var dbml = """
				Project project_name {
				  database_type: 'PostgreSQL'
				  Note: 'Description of the project'
				}""";
		var database = parse(dbml);
		
		System.out.println(database);
	}
}