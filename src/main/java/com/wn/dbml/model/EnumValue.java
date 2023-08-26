package com.wn.dbml.model;

import java.util.Objects;

public class EnumValue {
	private final Enum anEnum;
	private final String name;
	private Note note;
	
	EnumValue(final Enum anEnum, final String name) {
		this.anEnum = anEnum;
		this.name = name;
	}
	
	public Note getNote() {
		return note;
	}
	
	public void setNote(final Note note) {
		this.note = note;
	}
	
	@Override
	public boolean equals(final Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		final EnumValue enumValue = (EnumValue) o;
		return Objects.equals(name, enumValue.name);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name);
	}
	
	@Override
	public String toString() {
		return Name.of(anEnum, name);
	}
}
