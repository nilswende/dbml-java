package com.wn.dbml.compiler.lexer;

final class Char {
	private Char() {
	}
	
	public static boolean isDigit(final int c) {
		return '0' <= c && c <= '9';
	}
	
	public static boolean isLetter(final int c) {
		return 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z';
	}
	
	public static boolean isLinebreak(final int c) {
		return c == '\n' || c == '\r';
	}
	
	public static boolean isWordChar(final int c) {
		return isLetter(c) || isDigit(c) || c == '_';
	}
	
	public static boolean isHexDigit(final int c) {
		return '0' <= c && c <= '9' || 'a' <= c && c <= 'f' || 'A' <= c && c <= 'F';
	}
}
