package com.wn.dbml.compiler.token;

import com.wn.dbml.compiler.Token;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The type of {@link Token}.
 */
public enum TokenType {
	_KEYWORDS_START,
	PROJECT,
	TABLE,
	AS,
	REF,
	ENUM,
	TABLEGROUP,
	TABLEPARTIAL,
	HEADERCOLOR,
	COLOR,
	NOTE,
	PRIMARY,
	KEY,
	PK,
	NULL,
	NOT,
	UNIQUE,
	DEFAULT,
	INCREMENT,
	INDEXES,
	BTREE,
	HASH,
	TYPE,
	NAME,
	DELETE,
	UPDATE,
	CASCADE,
	RESTRICT,
	SET,
	NO,
	ACTION,
	_KEYWORDS_STOP,
	MINUS,
	LT,
	GT,
	NE,
	LPAREN,
	LBRACK,
	LBRACE,
	RPAREN,
	RBRACK,
	RBRACE,
	COLON,
	COMMA,
	DOT,
	TILDE,
	_WHITESPACE_START,
	LINEBREAK,
	SPACE,
	_WHITESPACE_STOP,
	_LITERAL_START,
	LITERAL,
	BOOLEAN,
	NUMBER,
	_LITERAL_STOP,
	SSTRING,
	DSTRING,
	TSTRING,
	EXPR,
	COLOR_CODE,
	COMMENT,
	// Misc
	EOF,
	ILLEGAL;
	
	/**
	 * The normalized separator for multi-word keywords.
	 */
	public static final String MULTI_SEPARATOR = " ";
	private static final Map<String, TokenType> KEYWORDS;
	
	static {
		var values = TokenType.values();
		KEYWORDS = Arrays.stream(values)
				.filter(TokenType::isKeyword)
				.collect(Collectors.toMap(TokenType::name, Function.identity()));
	}
	
	/**
	 * Returns a TokenType matching the word.
	 *
	 * @param word a word
	 * @return a keyword or else {@link TokenType#LITERAL}
	 */
	public static TokenType of(String word) {
		return KEYWORDS.getOrDefault(normalize(word), TokenType.LITERAL);
	}
	
	static String normalize(String word) {
		return word.toUpperCase();
	}
	
	/**
	 * Returns true, if this token represents a keyword.
	 */
	public boolean isKeyword() {
		return TokenType._KEYWORDS_START.ordinal() < ordinal() && ordinal() < TokenType._KEYWORDS_STOP.ordinal();
	}
	
	/**
	 * Returns true, if this token represents whitespace.
	 */
	public boolean isWhitespace() {
		return TokenType._WHITESPACE_START.ordinal() < ordinal() && ordinal() < TokenType._WHITESPACE_STOP.ordinal();
	}
	
	/**
	 * Returns true, if this token represents a literal.
	 */
	public boolean isLiteral() {
		return TokenType._LITERAL_START.ordinal() < ordinal() && ordinal() < TokenType._LITERAL_STOP.ordinal();
	}
}
