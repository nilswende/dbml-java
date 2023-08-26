package com.wn.dbml.compiler;

import com.wn.dbml.compiler.token.TokenType;

/**
 * An element of a DBML text.
 */
public interface Token {
	/**
	 * The type of this Token.
	 */
	TokenType getType();
	
	/**
	 * The value of this Token.
	 */
	String getValue();
	
	/**
	 * Returns a copy of this Token with {@link TokenType#LITERAL}.
	 */
	Token toLiteral();
}
