package com.wn.dbml.compiler.lexer;

final class Char {
	private Char() {
	}
	
	public static boolean isDigit(int c) {
		return '0' <= c && c <= '9';
	}
	
	public static boolean isLetter(int c) {
		return 'a' <= c && c <= 'z' || 'A' <= c && c <= 'Z';
	}
	
	public static boolean isLinebreak(int c) {
		return c == '\n' || c == '\r';
	}
	
	public static boolean isWordChar(int c) {
		return isLetter(c) || isDigit(c) || c == '_';
	}
	
	public static boolean isHexDigit(int c) {
		return '0' <= c && c <= '9' || 'a' <= c && c <= 'f' || 'A' <= c && c <= 'F';
	}
}
