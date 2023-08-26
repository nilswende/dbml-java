package com.wn.dbml.compiler.lexer;

import com.wn.dbml.compiler.token.TokenType;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class LexerImplTest {
	
	@Test
	@Disabled
	void printTokens() {
		var dbml = """
				Table users {
				     id integer
				     username varchar
				     role varchar
				     created_at timestamp
				 }
				 
				 Table posts {
				     id integer [primary key]
				     title varchar
				     body text [note: 'Content of the post']
				     user_id integer
				     created_at timestamp
				 }
				 
				 Ref: posts.user_id > users.id // many-to-one
				""";
		var lexer = new LexerImpl(dbml);
		
		lexer.tokenStream().forEach(t -> {
			if (t.getType() == TokenType.LINEBREAK) System.out.println();
			else {
				System.out.print(t);
				System.out.print(" ");
			}
		});
	}
}