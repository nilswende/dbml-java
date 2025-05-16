package com.wn.dbml.compiler.token;

import java.util.Set;
import java.util.regex.Pattern;

/**
 * Helper for literals.
 */
public final class Literals {
	private static final Set<String> BOOLEANS = Set.of("true", "false", "null");
	private static final Pattern NUMBERS = Pattern.compile("-?\\d+|-?\\d+\\.\\d+");
	
	private Literals() {
		throw new AssertionError();
	}
	
	/**
	 * Returns the string's literal subtype, if applicable.
	 *
	 * @param value a string
	 * @return the subtype or else null
	 */
	public static TokenType getSubType(String value) {
		if (isBooleanLiteral(value)) {
			return TokenType.BOOLEAN;
		} else if (isNumberLiteral(value)) {
			return TokenType.NUMBER;
		}
		return null;
	}
	
	/**
	 * Returns true, if the string is a boolean literal.
	 *
	 * @param value a string
	 */
	public static boolean isBooleanLiteral(String value) {
		return value != null && BOOLEANS.contains(value);
	}
	
	/**
	 * Returns true, if the string is a number literal.
	 *
	 * @param value a string
	 */
	public static boolean isNumberLiteral(String value) {
		return value != null && NUMBERS.matcher(value).matches();
	}
	
	/**
	 * Returns true, if the string is of any literal subtype.
	 *
	 * @param value a string
	 */
	public static boolean isSubType(String value) {
		return getSubType(value) != null;
	}
}
