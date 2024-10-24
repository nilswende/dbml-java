package com.wn.dbml.compiler.lexer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class MultiLineStringBuilder {
	private final String linebreak;
	private final List<String> lines = new ArrayList<>();
	
	public MultiLineStringBuilder() {
		this(System.lineSeparator());
	}
	
	public MultiLineStringBuilder(String linebreak) {
		this.linebreak = linebreak;
	}
	
	public void appendLine(String line) {
		lines.add(line);
	}
	
	@Override
	public String toString() {
		List<String> lines = new ArrayList<>(this.lines);
		removeLeadingSpaces(lines);
		lines = dropSurroundingBlankLines(lines);
		return String.join(linebreak, lines);
	}
	
	private void removeLeadingSpaces(List<String> lines) {
		lines.stream()
				.filter(l -> !l.isBlank())
				.mapToInt(this::countLeadingSpaces)
				.min()
				.ifPresent(minLeadingSpaces -> lines.replaceAll(l -> l.substring(Math.min(minLeadingSpaces, l.length()))));
	}
	
	private int countLeadingSpaces(String line) {
		int i = 0;
		while (i < line.length() && line.charAt(i) == ' ') {
			i++;
		}
		return i;
	}
	
	private List<String> dropSurroundingBlankLines(List<String> lines) {
		var list = lines;
		list = dropLeadingBlankLines(list);
		Collections.reverse(list);
		list = dropLeadingBlankLines(list);
		Collections.reverse(list);
		return list;
	}
	
	private List<String> dropLeadingBlankLines(List<String> lines) {
		return lines.stream()
				.dropWhile(String::isBlank)
				.collect(Collectors.toCollection(ArrayList::new));
	}
}
