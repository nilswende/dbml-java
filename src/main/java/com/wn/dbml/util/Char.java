package com.wn.dbml.util;

public final class Char {
	private Char() {
	}
	
	public static boolean isLinebreak(int c) {
		return c == '\n' || c == '\r';
	}
	
	public static boolean isWordChar(int c) {
		return Character.isLetter(c) || isDigit(c) || c == '_';
	}
	
	public static boolean isDigit(int c) {
		return '0' <= c && c <= '9';
	}
	
	public static boolean isHexDigit(int c) {
		return isDigit(c) || 'a' <= c && c <= 'f' || 'A' <= c && c <= 'F';
	}
}
