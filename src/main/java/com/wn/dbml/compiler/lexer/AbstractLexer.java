package com.wn.dbml.compiler.lexer;

import com.wn.dbml.compiler.Lexer;
import com.wn.dbml.compiler.Position;
import com.wn.dbml.compiler.Token;
import com.wn.dbml.compiler.token.TokenType;

import java.io.Reader;
import java.io.StringReader;

abstract class AbstractLexer implements Lexer {
	protected final LookaheadReader reader;
	private boolean ended;
	
	public AbstractLexer(final String string) {
		this(new StringReader(string));
	}
	
	public AbstractLexer(final Reader reader) {
		this.reader = new LookaheadReader(reader);
	}
	
	@Override
	public Token nextToken() {
		if (ended) return null;
		var token = nextTokenImpl();
		if (isEndToken(token)) {
			ended = true;
		}
		return token;
	}
	
	private boolean isEndToken(final Token token) {
		var type = token.getType();
		return type == TokenType.EOF || type == TokenType.ILLEGAL;
	}
	
	protected abstract Token nextTokenImpl();
	
	@Override
	public Position getPosition() {
		return reader.getPosition();
	}
	
	@Override
	public String toString() {
		return getPosition().toString();
	}
}
