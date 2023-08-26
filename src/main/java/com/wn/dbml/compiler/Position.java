package com.wn.dbml.compiler;

/**
 * A text position identified by its line and column.
 */
public record Position(
		int line, int column
) {
	@Override
	public String toString() {
		return String.format("[%d:%d]", line, column);
	}
}
