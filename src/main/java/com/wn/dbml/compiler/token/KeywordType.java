package com.wn.dbml.compiler.token;

import com.wn.dbml.compiler.Token;

import java.util.HashMap;
import java.util.Map;

/**
 * The type of keyword {@link Token}.
 */
public enum KeywordType {
	YES, NO, MULTI;
	
	private static final Map<String, KeywordType> KEYWORDS;
	
	static {
		var multiSeparator = String.valueOf(TokenType.MULTI_SEPARATOR_ENUM);
		var values = TokenType.values();
		var keywords = new HashMap<String, KeywordType>();
		for (int i = TokenType._KEYWORDS_START.ordinal() + 1; i < TokenType._KEYWORDS_STOP.ordinal(); i++) {
			var name = values[i].name();
			var isMultiWord = name.contains(multiSeparator);
			if (isMultiWord) {
				for (var word : name.split(multiSeparator)) {
					// the first word of a multi keyword cannot also be a single keyword
					keywords.putIfAbsent(word, KeywordType.MULTI);
				}
			}
			keywords.put(name, KeywordType.YES);
		}
		KEYWORDS = keywords;
	}
	
	/**
	 * Returns the KeywordType matching the word.
	 * If it is a keyword, {@link #YES} is returned.
	 * Else if it is part of a multi-word keyword, {@link #MULTI} is returned.
	 * Else {@link #NO} is returned.
	 *
	 * @param word a word
	 */
	public static KeywordType of(String word) {
		return KEYWORDS.getOrDefault(TokenType.normalize(word), KeywordType.NO);
	}
}
