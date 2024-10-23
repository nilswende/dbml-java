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
	 * Returns a copy of this Token with the given {@link TokenType}.
	 */
	Token withType(TokenType tokenType);
	
	/**
	 * Returns a copy of this Token with the matching literal type.
	 */
	default Token toLiteral() {
		return withType(TokenType.LITERAL);
	}
}
