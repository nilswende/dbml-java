package com.wn.dbml.compiler.lexer;

final class Char {
	private Char() {
	}
	
	public static boolean isLinebreak(int c) {
		return c == '\n' || c == '\r';
	}
	
	public static boolean isWordChar(int c) {
		return Character.isLetter(c) || '0' <= c && c <= '9' || c == '_';
	}
	
	public static boolean isHexDigit(int c) {
		return '0' <= c && c <= '9' || 'a' <= c && c <= 'f' || 'A' <= c && c <= 'F';
	}
}
