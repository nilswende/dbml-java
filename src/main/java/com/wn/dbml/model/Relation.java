package com.wn.dbml.model;

import java.util.Arrays;

public enum Relation {
	ONE_TO_MANY("<"),
	MANY_TO_ONE(">"),
	ONE_TO_ONE("-"),
	MANY_TO_MANY("<>");
	private final String symbol;
	
	Relation(final String symbol) {
		this.symbol = symbol;
	}
	
	public static Relation of(final String symbol) {
		return Arrays.stream(values()).filter(r -> r.symbol.equals(symbol)).findAny().orElse(null);
	}
	
	public String getSymbol() {
		return symbol;
	}
	
	@Override
	public String toString() {
		return symbol;
	}
}
