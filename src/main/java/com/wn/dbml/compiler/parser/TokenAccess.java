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
	
	TokenAccess(Lexer lexer) {
		this.lexer = Objects.requireNonNull(lexer);
	}
	
	public void next(TokenType... types) {
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
	
	private boolean skipToken(Token token) {
		return token != null && (
				// skip
				token.getType() == COMMENT
						// skip or collapse
						|| token.getType() == LINEBREAK && (ignoreLinebreaks || typeIs(LINEBREAK))
						// skip or collapse
						|| token.getType() == SPACE && (ignoreSpaces || typeIs(SPACE))
		);
	}
	
	private boolean shouldParseAsLiteral(TokenType... types) {
		return !type().isWhitespace()
				&& Arrays.stream(types).noneMatch(this::typeIs)
				&& Arrays.stream(types).anyMatch(TokenType::isLiteral);
	}
	
	private Token nextLiteral(TokenType... types) {
		var typeSet = Set.of(types);
		if (typeSet.contains(BOOLEAN)) {
			if (Literals.isBooleanLiteral(value())) {
				return token.withType(BOOLEAN);
			}
		}
		if (typeSet.contains(NUMBER)) {
			var peek = doLookahead();
			if (typeIs(MINUS) && peek.getType() == NUMBER) {
				var minus = value();
				token = nextToken();
				var number = value();
				return new TokenImpl(NUMBER, minus + number);
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
	
	public boolean lookaheadTypeIs(TokenType type) {
		return lookahead().getType() == type;
	}
	
	public boolean typeIs(TokenType type) {
		return type() == type;
	}
	
	public boolean typeIs(TokenType... types) {
		return Arrays.stream(types).anyMatch(this::typeIs);
	}
	
	public void expecting(Token token, TokenType... types) {
		var any = Arrays.stream(types).filter(t -> t == token.getType()).findAny();
		if (any.isEmpty() && types.length > 0) {
			expected(types);
		}
	}
	
	public void expected(TokenType... types) {
		var expected = Arrays.stream(types).map(Objects::toString).collect(Collectors.joining(", "));
		error("unexpected token '%s', expected %s. Last tokens: %s", type(), expected, lastTokens);
	}
	
	public void error(String msg, Object... args) {
		error(String.format(msg, args));
	}
	
	public void error(String msg) {
		throw new ParsingException(position(), msg);
	}
	
	public Position position() {
		return lookahead.isEmpty() ? lexer.getPosition() : lookahead.peek().position();
	}
	
	public void setIgnoreLinebreaks(boolean ignoreLinebreaks) {
		this.ignoreLinebreaks = ignoreLinebreaks;
	}
	
	public void setIgnoreSpaces(boolean ignoreSpaces) {
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
