package com.wn.dbml.compiler;

/**
 * Thrown to indicate that a {@link Parser} found unexpected input.
 */
public class ParsingException extends RuntimeException {
	private final Position position;
	
	public ParsingException(final Position position, final String msg) {
		super(position + " " + msg);
		this.position = position;
	}
	
	public Position getPosition() {
		return position;
	}
}
