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
	HEADERCOLOR,
	NOTE,
	PRIMARY_KEY,
	PK,
	NULL,
	NOT_NULL,
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
	SET_NULL,
	SET_DEFAULT,
	NO_ACTION,
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
	_WHITESPACE_START,
	LINEBREAK,
	SPACE,
	_WHITESPACE_STOP,
	// Literal
	LITERAL,
	SSTRING,
	DSTRING,
	TSTRING,
	EXPR,
	COLOR,
	COMMENT,
	// Misc
	EOF,
	ILLEGAL;
	
	/**
	 * The separator for multi-word keywords in DBML.
	 */
	static final char MULTI_SEPARATOR = ' ';
	/**
	 * The separator for multi-word keywords in this enum.
	 */
	static final char MULTI_SEPARATOR_ENUM = '_';
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
	public static TokenType of(final String word) {
		return KEYWORDS.getOrDefault(normalize(word), TokenType.LITERAL);
	}
	
	static String normalize(final String word) {
		return word.toUpperCase().replace(TokenType.MULTI_SEPARATOR, TokenType.MULTI_SEPARATOR_ENUM);
	}
	
	/**
	 * Returns true, if the string is the separator for multi-word keywords in DBML.
	 *
	 * @param string a string
	 */
	public static boolean isMultiSeparator(final String string) {
		return string.length() == 1 && string.charAt(0) == MULTI_SEPARATOR;
	}
	
	/**
	 * Returns true, if this token represents a keyword.
	 */
	public boolean isKeyword() {
		return TokenType._KEYWORDS_START.ordinal() < ordinal() && ordinal() < TokenType._KEYWORDS_STOP.ordinal();
	}
	
	/**
	 * Returns true, if this token represents a multi-word keyword.
	 */
	public boolean isMultiKeyword() {
		return isKeyword() && name().indexOf(MULTI_SEPARATOR_ENUM) != -1;
	}
	
	/**
	 * Returns true, if this token represents whitespace.
	 */
	public boolean isWhitespace() {
		return TokenType._WHITESPACE_START.ordinal() < ordinal() && ordinal() < TokenType._WHITESPACE_STOP.ordinal();
	}
}
