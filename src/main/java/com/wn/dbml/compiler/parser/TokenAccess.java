package com.wn.dbml.compiler.parser;

import com.wn.dbml.compiler.Lexer;
import com.wn.dbml.compiler.ParsingException;
import com.wn.dbml.compiler.Position;
import com.wn.dbml.compiler.Token;
import com.wn.dbml.compiler.token.Literals;
import com.wn.dbml.compiler.token.TokenImpl;
import com.wn.dbml.compiler.token.TokenType;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wn.dbml.compiler.token.TokenType.*;

class TokenAccess {
	private final RingBuffer<Token> lastTokens = new RingBuffer<>(5);
	private final Queue<Lookahead> lookahead = new ArrayDeque<>(2);
	private final Lexer lexer;
	private Token token;
	private boolean ignoreLinebreaks = true, ignoreSpaces = true;
	
	TokenAccess(final Lexer lexer) {
		this.lexer = Objects.requireNonNull(lexer);
	}
	
	public void next(final TokenType... types) {
		if (types != null && types.length > 0) {
			token = nextToken();
			if (shouldParseAsLiteral(types)) {
				token = nextLiteral(types);
			}
			lastTokens.add(token);
			expecting(token, types);
		}
	}
	
	private Token nextToken() {
		if (!lookahead.isEmpty()) {
			return lookahead.poll().token();
		}
		return nextTokenFromLexer();
	}
	
	private Token nextTokenFromLexer() {
		Token token;
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
	
	private boolean shouldParseAsLiteral(TokenType[] types) {
		return !type().isWhitespace() && !type().isMultiKeyword()
				&& Arrays.stream(types).noneMatch(this::typeIs)
				&& Arrays.stream(types).anyMatch(TokenType::isLiteral);
	}
	
	private Token nextLiteral(final TokenType... types) {
		var typeSet = Set.of(types);
		if (typeSet.contains(BLITERAL)) {
			if (Literals.isBooleanLiteral(value())) {
				return token.withType(BLITERAL);
			}
		}
		if (typeSet.contains(NLITERAL)) {
			var value = value();
			var peek = doLookahead();
			if (peek.getType() == DOT) {
				value += peek.getValue();
				peek = doLookahead();
				if (peek.getType() == LITERAL) {
					value += peek.getValue();
					if (Literals.isNumberLiteral(value)) {
						nextToken();
						nextToken();
						return new TokenImpl(NLITERAL, value);
					}
				}
			} else {
				if (Literals.isNumberLiteral(value)) {
					return token.withType(NLITERAL);
				}
			}
		}
		return token.toLiteral();
	}
	
	public Token lookahead() {
		if (lookahead.isEmpty()) {
			return doLookahead();
		}
		return lookahead.peek().token();
	}
	
	private Token doLookahead() {
		// save the current lexer position because nextToken() advances the lexer
		var position = lexer.getPosition();
		var t = nextTokenFromLexer();
		lookahead.add(new Lookahead(t, position));
		return t;
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
		return lookahead.isEmpty() ? lexer.getPosition() : lookahead.peek().position();
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
