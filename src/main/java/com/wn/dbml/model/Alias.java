package com.wn.dbml.model;

import com.wn.dbml.Name;

import java.util.Objects;

public class Alias {
	private final String name;
	
	public Alias(String name) {
		this.name = Objects.requireNonNull(name);
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return Name.of(name);
	}
}
