package com.wn.dbml.compiler.lexer;

import com.wn.dbml.compiler.Position;

import java.io.*;
import java.util.Objects;

class LookaheadReader {
	private static final int DEFAULT_LOOKAHEAD_BUFFER_SIZE = 64;
	private final PushbackReader reader;
	private int line = 1, column = 0;
	private boolean wasLinebreak;
	
	public LookaheadReader(final Reader reader) {
		this(reader, DEFAULT_LOOKAHEAD_BUFFER_SIZE);
	}
	
	public LookaheadReader(final Reader reader, final int size) {
		if (size < DEFAULT_LOOKAHEAD_BUFFER_SIZE) throw new IllegalArgumentException("Cannot decrease the buffer size");
		var buffered = Objects.requireNonNull(reader) instanceof BufferedReader ? reader : new BufferedReader(reader);
		this.reader = new PushbackReader(buffered, size);
	}
	
	public int nextChar() {
		var next = read();
		if (next == -1) {
			return next;
		}
		if (wasLinebreak) {
			line++;
			column = 0;
			wasLinebreak = false;
		}
		if (Char.isLinebreak(next)) {
			if (next == '\r' && lookahead() == '\n') {
				// collapse \r\n
				return nextChar();
			}
			wasLinebreak = true;
		}
		column++;
		return next;
	}
	
	private int read() {
		try {
			return this.reader.read();
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public int lookahead() {
		var c = read();
		if (c != -1) {
			pushback(c);
		}
		return c;
	}
	
	public String lookahead(final int length) {
		var sb = new StringBuilder();
		for (int i = 0; i < length; i++) {
			var c = read();
			if (c == -1) break;
			sb.append((char) c);
		}
		var chars = sb.toString();
		pushback(chars);
		return chars;
	}
	
	public void pushback(final int c) {
		try {
			reader.unread(c);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void pushback(final String chars) {
		try {
			reader.unread(chars.toCharArray());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public Position getPosition() {
		return new Position(line, column);
	}
	
	@Override
	public String toString() {
		return getPosition().toString();
	}
}
