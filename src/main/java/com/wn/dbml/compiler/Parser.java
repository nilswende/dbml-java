package com.wn.dbml.compiler;

import com.wn.dbml.model.Database;

/**
 * Creates a {@link Database} using a {@link Lexer}.
 */
public interface Parser {
	/**
	 * Creates a database using the lexer.
	 *
	 * @param lexer a lexer
	 */
	Database parse(Lexer lexer);
}
