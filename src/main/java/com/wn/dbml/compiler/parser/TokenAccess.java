package com.wn.dbml.compiler.parser;

import com.wn.dbml.compiler.Lexer;
import com.wn.dbml.compiler.ParsingException;
import com.wn.dbml.compiler.Position;
import com.wn.dbml.compiler.Token;
import com.wn.dbml.compiler.token.TokenType;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.wn.dbml.compiler.token.TokenType.*;

class TokenAccess {
	private final RingBuffer<Token> lastTokens = new RingBuffer<>(5);
	private final Lexer lexer;
	private Token token;
	private Lookahead lookahead;
	private boolean ignoreLinebreaks = true, ignoreSpaces = true;
	
	TokenAccess(final Lexer lexer) {
		this.lexer = Objects.requireNonNull(lexer);
	}
	
	public void next(final TokenType... types) {
		if (types != null && types.length > 0) {
			token = nextToken();
			if (!type().isWhitespace() && !type().isMultiKeyword()
				&& Arrays.stream(types).noneMatch(this::typeIs)
				&& Arrays.stream(types).anyMatch(t -> t == LITERAL)) {
				token = token.toLiteral();
			}
			lastTokens.add(token);
			expecting(token, types);
		}
	}
	
	private Token nextToken() {
		Token token;
		if (lookahead != null) {
			token = lookahead.token();
			lookahead = null;
			return token;
		}
		do {
			token = lexer.nextToken();
		} while (skipToken(token));
		return token;
	}
	
	private boolean skipToken(final Token token) {
		return token != null && (
				// skip
				token.getType() == COMMENT
				// skip or collapse
				|| token.getType() == LINEBREAK && (ignoreLinebreaks || typeIs(LINEBREAK))
				// skip or collapse
				|| token.getType() == SPACE && (ignoreSpaces || typeIs(SPACE))
		);
	}
	
	public Token lookahead() {
		if (lookahead == null) {
			// save the current token position because nextToken() advances the lexer
			var position = position();
			lookahead = new Lookahead(nextToken(), position);
		}
		return lookahead.token();
	}
	
	public TokenType type() {
		return token.getType();
	}
	
	public String value() {
		return token.getValue();
	}
	
	public boolean lookaheadTypeIs(final TokenType type) {
		return lookahead().getType() == type;
	}
	
	public boolean typeIs(final TokenType type) {
		return type() == type;
	}
	
	public boolean typeIs(final TokenType... types) {
		return Arrays.stream(types).anyMatch(this::typeIs);
	}
	
	public void expecting(final Token token, final TokenType... types) {
		var any = Arrays.stream(types).filter(t -> t == token.getType()).findAny();
		if (any.isEmpty() && types.length > 0) {
			expected(types);
		}
	}
	
	public void expected(final TokenType... types) {
		var expected = Arrays.stream(types).map(Objects::toString).collect(Collectors.joining(", "));
		error("unexpected token '%s', expected %s. Last tokens: %s", type(), expected, lastTokens);
	}
	
	public void error(final String msg, final Object... args) {
		error(String.format(msg, args));
	}
	
	public void error(final String msg) {
		throw new ParsingException(position(), msg);
	}
	
	public Position position() {
		return lookahead == null ? lexer.getPosition() : lookahead.position();
	}
	
	public void setIgnoreLinebreaks(final boolean ignoreLinebreaks) {
		this.ignoreLinebreaks = ignoreLinebreaks;
	}
	
	public void setIgnoreSpaces(final boolean ignoreSpaces) {
		this.ignoreSpaces = ignoreSpaces;
	}
	
	@Override
	public String toString() {
		return position() + " " + token;
	}
	
	private record Lookahead(
			Token token, Position position
	) {
	}
}
