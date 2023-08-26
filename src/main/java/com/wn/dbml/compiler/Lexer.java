package com.wn.dbml.compiler;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Breaks down a DBML text into {@link Token}s.
 *
 * @see Parser
 */
public interface Lexer {
	/**
	 * The next token from the text.
	 *
	 * @return the next token or null, if there is no next token.
	 */
	Token nextToken();
	
	/**
	 * The current position of the lexer in the text it is reading.
	 */
	Position getPosition();
	
	/**
	 * A stream of the remaining tokens in the text, starting with the {@link #nextToken()}.
	 */
	default Stream<Token> tokenStream() {
		return Stream.iterate(nextToken(), Objects::nonNull, t -> nextToken());
	}
	
	/**
	 * A list of the remaining tokens in the text, starting with the {@link #nextToken()}.
	 */
	default List<Token> tokenList() {
		return tokenStream().toList();
	}
}
