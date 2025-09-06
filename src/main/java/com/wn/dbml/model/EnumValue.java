package com.wn.dbml.model;

import com.wn.dbml.Name;

import java.util.Objects;

public class EnumValue {
	private final Enum anEnum;
	private final String name;
	private Note note;
	
	EnumValue(Enum anEnum, String name) {
		this.anEnum = Objects.requireNonNull(anEnum);
		this.name = Name.requireNonEmpty(name);
	}
	
	public Enum getEnum() {
		return anEnum;
	}
	
	public String getName() {
		return name;
	}
	
	public Note getNote() {
		return note;
	}
	
	public void setNote(Note note) {
		this.note = note;
	}
	
	@Override
	public boolean equals(Object o) {
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
		return Name.of(name);
	}
}
