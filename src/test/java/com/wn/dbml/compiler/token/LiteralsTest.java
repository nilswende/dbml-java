package com.wn.dbml.compiler.token;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class LiteralsTest {
	
	@ParameterizedTest
	@MethodSource
	void isSubType(String input, Boolean expected) {
		assertEquals(expected, Literals.isSubType(input));
	}
	
	static Stream<Arguments> isSubType() {
		return Stream.of(
				arguments(null, false),
				arguments("", false),
				arguments("true", true),
				arguments("false", true),
				arguments("null", true)
		);
	}
}