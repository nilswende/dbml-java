package com.wn.dbml.compiler.lexer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;

class MultiLineStringBuilderTest {
	
	@ParameterizedTest
	@MethodSource
	void test(List<String> lines, String expected) {
		var mlsb = new MultiLineStringBuilder("\n");
		lines.forEach(mlsb::appendLine);
		var actual = mlsb.toString();
		Assertions.assertEquals(expected, actual);
	}
	
	static Stream<Arguments> test() {
		return Stream.of(
				arguments(List.of(), ""),
				arguments(List.of(""), ""),
				arguments(List.of("a"), "a"),
				arguments(List.of(" ", "a", " "), "a"),
				arguments(List.of(" ", " a", "  b", " "), "a\n b")
		);
	}
}