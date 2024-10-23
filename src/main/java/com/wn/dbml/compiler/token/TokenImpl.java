package com.wn.dbml.compiler.token;

import com.wn.dbml.compiler.Token;

/**
 * The default token implementation.
 */
public class TokenImpl implements Token {
	private final TokenType type;
	private final String value;
	
	public TokenImpl(final TokenType type, final int value) {
		this(type, value < 0 ? "" : Character.toString(value));
	}
	
	public TokenImpl(final String value) {
		this(TokenType.of(value), value);
	}
	
	public TokenImpl(final TokenType type, final String value) {
		this.type = type;
		this.value = value;
	}
	
	@Override
	public Token withType(final TokenType tokenType) {
		return new TokenImpl(tokenType, value);
	}
	
	@Override
	public String toString() {
		return type + "('" + value + "')";
	}
	
	@Override
	public TokenType getType() {
		return type;
	}
	
	@Override
	public String getValue() {
		return value;
	}
}
