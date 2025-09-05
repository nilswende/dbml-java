package com.wn.dbml;

import java.util.Arrays;

public final class Chars {
	public static final String EMPTY = "";
	
	private Chars() {
	}
	
	public static boolean hasLinebreak(String s) {
		return s.chars().anyMatch(Char::isLinebreak);
	}
	
	public static boolean isWordChars(String s) {
		return s.chars().allMatch(Char::isWordChar);
	}
	
	public static boolean isNumber(String s) {
		var split = s.split("\\.");
		return 1 <= split.length && split.length <= 2 && Arrays.stream(split).flatMapToInt(String::chars).allMatch(Char::isDigit);
	}
	
	public static boolean isHexDigits(String s) {
		return s.chars().allMatch(Char::isHexDigit);
	}
}
