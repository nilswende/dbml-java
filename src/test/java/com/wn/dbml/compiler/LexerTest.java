package com.wn.dbml.compiler;

import com.wn.dbml.compiler.lexer.LexerImpl;
import com.wn.dbml.compiler.token.TokenType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static com.wn.dbml.compiler.token.TokenType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class LexerTest {
	
	private Lexer getLexer(String dbml) {
		return new LexerImpl(dbml);
	}
	
	@ParameterizedTest
	@MethodSource
	void testName(String string, List<TokenType> expected) {
		var lexer = getLexer(string);
		
		var actual = lexer.tokenStream().map(Token::getType).toList();
		
		assertEquals(expected, actual);
	}
	
	static Stream<Arguments> testName() {
		return Stream.of(
				arguments("ab", List.of(LITERAL, EOF)),
				arguments("ab12", List.of(LITERAL, EOF)),
				arguments("ab12ab", List.of(LITERAL, EOF)),
				arguments("12ab", List.of(LITERAL, EOF)),
				arguments("_12", List.of(LITERAL, EOF)),
				arguments("1_2", List.of(LITERAL, EOF))
		);
	}
	
	@ParameterizedTest
	@MethodSource
	void testNumber(String string, List<TokenType> expected) {
		var lexer = getLexer(string);
		
		var actual = lexer.tokenStream().map(Token::getType).toList();
		
		assertEquals(expected, actual);
	}
	
	static Stream<Arguments> testNumber() {
		return Stream.of(
				arguments("12", List.of(NUMBER, EOF)),
				arguments("1.2", List.of(NUMBER, EOF)),
				arguments("1.", List.of(NUMBER, EOF)),
				arguments(".2", List.of(DOT, NUMBER, EOF)),
				arguments("1.word", List.of(NUMBER, LITERAL, EOF)),
				arguments("-12", List.of(MINUS, NUMBER, EOF)),
				arguments("- 1.2", List.of(MINUS, SPACE, NUMBER, EOF)),
				arguments("-1.", List.of(MINUS, NUMBER, EOF)),
				arguments("-.2", List.of(MINUS, DOT, NUMBER, EOF)),
				arguments("-1.word", List.of(MINUS, NUMBER, LITERAL, EOF))
		);
	}
	
	@Test
	void testKeyword() {
		var dbml = "table";
		var lexer = getLexer(dbml);
		
		var tokenList = lexer.tokenList();
		var types = tokenList.stream().map(Token::getType).toList();
		
		assertEquals(List.of(TABLE, EOF), types);
		assertEquals(dbml, tokenList.getFirst().getValue());
	}
	
	@ParameterizedTest
	@MethodSource
	void testString(char quote, TokenType expectedType) {
		var str = "test string \\ with unicode 倀";
		var dbml = quote + str + quote;
		var lexer = getLexer(dbml);
		
		var tokenList = lexer.tokenList();
		var types = tokenList.stream().map(Token::getType).toList();
		
		assertEquals(List.of(expectedType, EOF), types);
		assertEquals(str, tokenList.getFirst().getValue());
	}
	
	static Stream<Arguments> testString() {
		return Stream.of(
				arguments("'", SSTRING),
				arguments("\"", DSTRING),
				arguments("`", EXPR)
		);
	}
	
	@Test
	void testStringEscapedQuote() {
		var quote = "'";
		var str = "test string \\" + quote + " with unicode 倀";
		var dbml = quote + str + quote;
		var lexer = getLexer(dbml);
		
		var tokenList = lexer.tokenList();
		var types = tokenList.stream().map(Token::getType).toList();
		
		assertEquals(List.of(SSTRING, EOF), types);
		assertEquals(str.replace("\\", ""), tokenList.getFirst().getValue());
	}
	
	@Test
	void testStringEOF() {
		var dbml = "'test string with unicode 倀";
		var lexer = getLexer(dbml);
		
		var tokenList = lexer.tokenList();
		var types = tokenList.stream().map(Token::getType).toList();
		
		assertEquals(List.of(ILLEGAL), types);
	}
	
	@Test
	void testMultiLineString() {
		var dbml = """
				'''
				
				test string with unicode 倀
				  indented \\
				  continued \\\\
				  escaped \\'''
				  \\ inline
				end'''""";
		var lexer = getLexer(dbml);
		
		var tokenList = lexer.tokenList();
		var types = tokenList.stream().map(Token::getType).toList();
		
		assertEquals(List.of(TSTRING, EOF), types);
		assertEquals("""
				test string with unicode 倀
				  indented   continued \\
				  escaped '''
				  \\ inline
				end""", tokenList.getFirst().getValue());
	}
	
	@Test
	void testMultiLineStringEOF() {
		var dbml = """
				'''
				
				test string with unicode 倀
				  indented""";
		var lexer = getLexer(dbml);
		
		var tokenList = lexer.tokenList();
		var types = tokenList.stream().map(Token::getType).toList();
		
		assertEquals(List.of(ILLEGAL), types);
	}
	
	@Test
	void testComment() {
		var dbml = "//'test string with unicode 倀";
		var lexer = getLexer(dbml);
		
		var tokenList = lexer.tokenList();
		var types = tokenList.stream().map(Token::getType).toList();
		
		assertEquals(List.of(COMMENT, EOF), types);
	}
	
	@Test
	void testNotComment() {
		var dbml = "/'test string with unicode 倀";
		var lexer = getLexer(dbml);
		
		var tokenList = lexer.tokenList();
		var types = tokenList.stream().map(Token::getType).toList();
		
		assertEquals(List.of(ILLEGAL), types);
	}
	
	@Test
	void testMultiLineComment() {
		var dbml = """
				/*
				
				test string with unicode 倀
				  indented
				*/""";
		var lexer = getLexer(dbml);
		
		var tokenList = lexer.tokenList();
		var types = tokenList.stream().map(Token::getType).toList();
		
		assertEquals(List.of(COMMENT, EOF), types);
		assertEquals("""
				test string with unicode 倀
				  indented""", tokenList.getFirst().getValue());
	}
	
	@Test
	void testLinebreak() {
		var dbml = "\r\n'test string with unicode 倀'";
		var lexer = getLexer(dbml);
		
		var tokenList = lexer.tokenList();
		var types = tokenList.stream().map(Token::getType).toList();
		
		assertEquals(List.of(LINEBREAK, SSTRING, EOF), types);
		assertEquals(2, lexer.getPosition().line());
	}
	
	@Test
	void testLinebreakNoCollapsing() {
		var dbml = "\r\n\r\n'test string'";
		var lexer = getLexer(dbml);
		
		var tokenList = lexer.tokenList();
		var types = tokenList.stream().map(Token::getType).toList();
		
		assertEquals(List.of(LINEBREAK, LINEBREAK, SSTRING, EOF), types);
		assertEquals(3, lexer.getPosition().line());
		assertEquals(13, lexer.getPosition().column());
	}
	
	@Test
	void testSpaceNoCollapsing() {
		var dbml = "table  tbl";
		var lexer = getLexer(dbml);
		
		var tokenList = lexer.tokenList();
		var types = tokenList.stream().map(Token::getType).toList();
		
		assertEquals(List.of(TABLE, SPACE, SPACE, LITERAL, EOF), types);
		assertEquals(1, lexer.getPosition().line());
		assertEquals(10, lexer.getPosition().column());
	}
	
	@ParameterizedTest
	@MethodSource
	void testColor(String color, TokenType expectedType) {
		var dbml = "#" + color;
		var lexer = getLexer(dbml);
		
		var tokenList = lexer.tokenList();
		var types = tokenList.stream().map(Token::getType).toList();
		
		if (expectedType == COLOR_CODE) {
			assertEquals(List.of(expectedType, EOF), types);
			assertEquals(dbml, tokenList.getFirst().getValue());
		} else {
			assertEquals(List.of(expectedType), types);
		}
	}
	
	static Stream<Arguments> testColor() {
		return Stream.of(
				arguments(" ", ILLEGAL),
				arguments("0", ILLEGAL),
				arguments("x", ILLEGAL),
				arguments("xxx", ILLEGAL),
				arguments("xxxxxx", ILLEGAL),
				arguments("1 ab", ILLEGAL),
				arguments("1a b2cd", ILLEGAL),
				arguments("1ab", COLOR_CODE),
				arguments("1ab2cd", COLOR_CODE)
		);
	}
	
	@Test
	void testUnknown() {
		var dbml = "&";
		var lexer = getLexer(dbml);
		
		var tokenList = lexer.tokenList();
		var types = tokenList.stream().map(Token::getType).toList();
		
		assertEquals(List.of(ILLEGAL), types);
	}
	
	@Test
	void testUmlaut() {
		var dbml = "table üser {";
		var lexer = getLexer(dbml);
		
		var tokenList = lexer.tokenList();
		var types = tokenList.stream().map(Token::getType).toList();
		
		assertEquals(List.of(TABLE, SPACE, LITERAL, SPACE, LBRACE, EOF), types);
		assertEquals(1, lexer.getPosition().line());
		assertEquals(12, lexer.getPosition().column());
	}
	
	@Test
	void testNonWordChar() {
		var dbml = "table ü&ser {";
		var lexer = getLexer(dbml);
		
		var tokenList = lexer.tokenList();
		var types = tokenList.stream().map(Token::getType).toList();
		
		assertEquals(List.of(TABLE, SPACE, LITERAL, ILLEGAL), types);
		assertEquals(1, lexer.getPosition().line());
		assertEquals(8, lexer.getPosition().column());
	}
	
	@Test
	void testEmptySString() {
		var dbml = "note: ''";
		var lexer = getLexer(dbml);
		
		var tokenList = lexer.tokenList();
		var types = tokenList.stream().map(Token::getType).toList();
		
		assertEquals(List.of(NOTE, COLON, SPACE, SSTRING, EOF), types);
		assertEquals(1, lexer.getPosition().line());
		assertEquals(8, lexer.getPosition().column());
	}
	
	@Test
	void testEmptyDString() {
		var dbml = "note: \"\"";
		var lexer = getLexer(dbml);
		
		var tokenList = lexer.tokenList();
		var types = tokenList.stream().map(Token::getType).toList();
		
		assertEquals(List.of(NOTE, COLON, SPACE, DSTRING, EOF), types);
		assertEquals(1, lexer.getPosition().line());
		assertEquals(8, lexer.getPosition().column());
	}
	
	@Test
	void testEmptyTString() {
		var dbml = "note: ''''''";
		var lexer = getLexer(dbml);
		
		var tokenList = lexer.tokenList();
		var types = tokenList.stream().map(Token::getType).toList();
		
		assertEquals(List.of(NOTE, COLON, SPACE, TSTRING, EOF), types);
		assertEquals(1, lexer.getPosition().line());
		assertEquals(12, lexer.getPosition().column());
	}
}