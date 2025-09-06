package com.wn.dbml.printer;

import com.wn.dbml.model.Column;
import com.wn.dbml.model.ColumnSetting;
import com.wn.dbml.model.Database;
import com.wn.dbml.model.Enum;
import com.wn.dbml.model.EnumValue;
import com.wn.dbml.model.Index;
import com.wn.dbml.model.IndexSetting;
import com.wn.dbml.model.NamedNote;
import com.wn.dbml.model.Project;
import com.wn.dbml.model.Relationship;
import com.wn.dbml.model.Schema;
import com.wn.dbml.model.Table;
import com.wn.dbml.model.TableGroup;
import com.wn.dbml.model.TablePartial;
import com.wn.dbml.util.Char;
import com.wn.dbml.util.Chars;
import com.wn.dbml.visitor.DatabaseVisitor;

import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Creates DBML from a database representation.
 */
public class DbmlPrinter implements DatabaseVisitor {
	private final StringBuilder sb = new StringBuilder();
	private final DbmlFormatter formatter;
	private int level = 0;
	
	public DbmlPrinter() {
		this(new DbmlFormatter.Builder().build());
	}
	
	public DbmlPrinter(DbmlFormatter formatter) {
		this.formatter = formatter;
	}
	
	private void println(Consumer<StringBuilder> line) {
		indent();
		line.accept(sb);
		println();
	}
	
	private void indent() {
		for (int i = 0; i < level; i++) {
			sb.append(formatter.getIndentation());
		}
	}
	
	private void println() {
		sb.append(formatter.getLinebreak());
	}
	
	private String quoteString(String s) {
		return Chars.hasLinebreak(s) ? "'''" + s + "'''" : '\'' + s + '\'';
	}
	
	private String quoteColumnType(String s) {
		return s.chars().allMatch(c -> Char.isWordChar(c) || c == '(' || c == ')') ? s : '"' + s + '"';
	}
	
	private String quoteColumnDefault(String s) {
		return s.equals("null") || Chars.isNumber(s) ? s : quoteString(s);
	}
	
	private void endLevel() {
		level--;
		println(sb -> sb.append('}'));
		println();
	}
	
	@Override
	public void visit(Column column) {
		println(sb -> sb.append(column).append(' ').append(quoteColumnType(column.getType())).append(columnSettings(column)));
	}
	
	private String columnSettings(Column column) {
		var list = new ArrayList<String>();
		if (!column.getSettings().isEmpty()) {
			column.getSettings().forEach((k, v) -> list.add(k == ColumnSetting.DEFAULT ? k + ": " + quoteColumnDefault(v) : k.toString()));
		}
		if (column.getNote() != null && !column.getNote().getValue().isBlank()) {
			list.add("note: " + quoteString(column.getNote().getValue()));
		}
		return list.isEmpty() ? Chars.EMPTY : list.stream().collect(Collectors.joining(", ", " [", "]"));
	}
	
	@Override
	public void visit(Database database) {
		if (database.getProject() != null) {
			database.getProject().accept(this);
		}
		database.getSchemas().stream().flatMap(s -> s.getEnums().stream()).forEach(e -> e.accept(this));
		database.getTablePartials().forEach(tp -> tp.accept(this));
		database.getSchemas().stream().flatMap(s -> s.getTables().stream()).forEach(t -> t.accept(this));
		database.getRelationships().forEach(r -> r.accept(this));
		database.getTableGroups().forEach(tg -> tg.accept(this));
		database.getNamedNotes().forEach(nn -> nn.accept(this));
	}
	
	@Override
	public void visit(Enum anEnum) {
		println(sb -> sb.append("enum ").append(anEnum).append(" {"));
		level++;
		anEnum.getValues().forEach(ev -> println(sb -> sb.append(ev).append(enumSettings(ev))));
		endLevel();
	}
	
	private String enumSettings(EnumValue value) {
		return value.getNote() != null && !value.getNote().getValue().isBlank() ? " [note: " + quoteString(value.getNote().getValue()) + ']' : Chars.EMPTY;
	}
	
	@Override
	public void visit(Index index) {
		println(sb -> sb.append(index).append(indexSettings(index)));
	}
	
	private String indexSettings(Index index) {
		var list = new ArrayList<String>();
		if (!index.getSettings().isEmpty()) {
			index.getSettings().forEach((k, v) -> list.add(v == null ? k.toString() : k + ": " + (k == IndexSetting.NAME ? quoteString(v) : v)));
		}
		if (index.getNote() != null && !index.getNote().getValue().isBlank()) {
			list.add("note: " + quoteString(index.getNote().getValue()));
		}
		return list.isEmpty() ? Chars.EMPTY : list.stream().collect(Collectors.joining(", ", " [", "]"));
	}
	
	@Override
	public void visit(NamedNote namedNote) {
		println(sb -> sb.append("Note ").append(namedNote.getName()).append(" {"));
		level++;
		println(sb -> sb.append(quoteString(namedNote.getValue())));
		endLevel();
	}
	
	@Override
	public void visit(Project project) {
		println(sb -> sb.append("Project ").append(project).append(" {"));
		level++;
		project.getProperties().forEach((k, v) -> println(sb -> sb.append(k).append(": '").append(v).append('\'')));
		if (project.getNote() != null && !project.getNote().getValue().isBlank()) {
			println();
			println(sb -> sb.append("Note: ").append(quoteString(project.getNote().getValue())));
		}
		endLevel();
	}
	
	@Override
	public void visit(Relationship relationship) {
		println(sb -> sb.append("Ref").append(relationshipName(relationship)).append(": ").append(relationship).append(relationshipSettings(relationship)));
		println();
	}
	
	private String relationshipName(Relationship relationship) {
		return relationship.getName() == null ? Chars.EMPTY : " " + relationship.getName();
	}
	
	private String relationshipSettings(Relationship relationship) {
		return relationship.getSettings().isEmpty() ? Chars.EMPTY
				: relationship.getSettings().entrySet().stream()
				.map(e -> e.getValue() == null ? e.getKey().toString() : e.getKey() + ": " + e.getValue())
				.collect(Collectors.joining(", ", " [", "]"));
	}
	
	@Override
	public void visit(Schema schema) {
		schema.getEnums().forEach(e -> e.accept(this));
		schema.getTables().forEach(t -> t.accept(this));
	}
	
	@Override
	public void visit(Table table) {
		printTable(table, "Table ");
	}
	
	private void printTable(Table table, String name) {
		println(sb -> sb.append(name).append(table).append(tableAlias(table)).append(tableSettings(table)).append(" {"));
		level++;
		table.getLocalTablePartials().forEach(tp -> println(sb -> sb.append('~').append(tp)));
		table.getLocalColumns().forEach(c -> c.accept(this));
		if (!table.getLocalIndexes().isEmpty()) {
			println();
			println(sb -> sb.append("indexes").append(" {"));
			level++;
			table.getLocalIndexes().forEach(i -> i.accept(this));
			level--;
			println(sb -> sb.append('}'));
		}
		if (table.getLocalNote() != null && !table.getLocalNote().getValue().isBlank()) {
			println();
			println(sb -> sb.append("Note: ").append(quoteString(table.getLocalNote().getValue())));
		}
		endLevel();
	}
	
	private String tableAlias(Table table) {
		return table.getAlias() == null ? Chars.EMPTY : " as " + table.getAlias();
	}
	
	private String tableSettings(Table table) {
		return table.getLocalSettings().isEmpty() ? Chars.EMPTY
				: table.getLocalSettings().entrySet().stream()
				.map(e -> e.getValue() == null ? e.getKey().toString() : e.getKey() + ": " + e.getValue())
				.collect(Collectors.joining(", ", " [", "]"));
	}
	
	@Override
	public void visit(TableGroup tableGroup) {
		println(sb -> sb.append("TableGroup ").append(tableGroup).append(tableGroupSettings(tableGroup)).append(" {"));
		level++;
		tableGroup.getTables().forEach(t -> println(sb -> sb.append(t.getAlias() == null ? t : t.getAlias())));
		if (tableGroup.getNote() != null && !tableGroup.getNote().getValue().isBlank()) {
			println();
			println(sb -> sb.append("Note: ").append(quoteString(tableGroup.getNote().getValue())));
		}
		endLevel();
	}
	
	private String tableGroupSettings(TableGroup tableGroup) {
		return tableGroup.getSettings().isEmpty() ? Chars.EMPTY
				: tableGroup.getSettings().entrySet().stream()
				.map(e -> e.getValue() == null ? e.getKey().toString() : e.getKey() + ": " + e.getValue())
				.collect(Collectors.joining(", ", " [", "]"));
	}
	
	@Override
	public void visit(TablePartial tablePartial) {
		printTable(tablePartial, "TablePartial ");
	}
	
	@Override
	public String toString() {
		var len = formatter.getLinebreak().length();
		return sb.delete(sb.length() - 2 * len, sb.length()).toString();
	}
}
