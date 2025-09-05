package com.wn.dbml.printer;

import com.wn.dbml.Char;
import com.wn.dbml.Chars;
import com.wn.dbml.model.Column;
import com.wn.dbml.model.ColumnSetting;
import com.wn.dbml.model.Database;
import com.wn.dbml.model.Enum;
import com.wn.dbml.model.Index;
import com.wn.dbml.model.IndexSetting;
import com.wn.dbml.model.NamedNote;
import com.wn.dbml.model.Project;
import com.wn.dbml.model.Relationship;
import com.wn.dbml.model.Schema;
import com.wn.dbml.model.Table;
import com.wn.dbml.model.TableGroup;
import com.wn.dbml.model.TablePartial;
import com.wn.dbml.visitor.DatabaseVisitor;

import java.util.ArrayList;
import java.util.function.UnaryOperator;
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
	
	private void printLine(UnaryOperator<StringBuilder> line) {
		indent();
		line.apply(sb);
		newline();
	}
	
	private void indent() {
		for (int i = 0; i < level; i++) {
			sb.append(formatter.getIndentation());
		}
	}
	
	private void newline() {
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
	
	@Override
	public void visit(Column column) {
		printLine(sb -> sb.append(column).append(' ').append(quoteColumnType(column.getType())).append(columnSettings(column)));
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
	
	}
	
	@Override
	public void visit(Index index) {
		printLine(sb -> sb.append(index).append(indexSettings(index)));
	}
	
	private String indexSettings(Index index) {
		var list = new ArrayList<String>();
		if (!index.getSettings().isEmpty()) {
			index.getSettings().forEach((k, v) -> {
				
				list.add(v == null ? k.toString() : k + ": " + (k == IndexSetting.NAME ? quoteString(v) : v));
			});
		}
		if (index.getNote() != null && !index.getNote().getValue().isBlank()) {
			list.add("note: " + quoteString(index.getNote().getValue()));
		}
		return list.isEmpty() ? Chars.EMPTY : list.stream().collect(Collectors.joining(", ", " [", "]"));
	}
	
	@Override
	public void visit(NamedNote namedNote) {
		printLine(sb -> sb.append("Note ").append(namedNote.getName()).append(" {"));
		level++;
		printLine(sb -> sb.append(quoteString(namedNote.getValue())));
		level--;
		sb.append('}');
	}
	
	@Override
	public void visit(Project project) {
		printLine(sb -> sb.append("Project ").append(project).append(" {"));
		level++;
		project.getProperties().forEach((k, v) -> printLine(sb -> sb.append(k).append(": '").append(v).append('\'')));
		if (project.getNote() != null && !project.getNote().getValue().isBlank()) {
			newline();
			printLine(sb -> sb.append("Note: ").append(quoteString(project.getNote().getValue())));
		}
		level--;
		sb.append('}');
	}
	
	@Override
	public void visit(Relationship relationship) {
	
	}
	
	@Override
	public void visit(Schema schema) {
		schema.getEnums().forEach(e -> e.accept(this));
		schema.getTables().forEach(t -> t.accept(this));
	}
	
	@Override
	public void visit(Table table) {
		printLine(sb -> sb.append("Table ").append(table).append(tableAlias(table)).append(tableSettings(table)).append(" {"));
		level++;
		table.getColumns().forEach(c -> c.accept(this));
		if (!table.getIndexes().isEmpty()) {
			newline();
			printLine(sb -> sb.append("indexes").append(" {"));
			level++;
			table.getIndexes().forEach(i -> i.accept(this));
			level--;
			printLine(sb -> sb.append('}'));
		}
		if (table.getNote() != null && !table.getNote().getValue().isBlank()) {
			newline();
			printLine(sb -> sb.append("Note: ").append(quoteString(table.getNote().getValue())));
		}
		level--;
		sb.append('}');
	}
	
	private String tableAlias(Table table) {
		return table.getAlias() == null ? Chars.EMPTY : " as " + table.getAlias();
	}
	
	private String tableSettings(Table table) {
		return table.getSettings().isEmpty() ? Chars.EMPTY
				: table.getSettings().entrySet().stream()
				.map(e -> e.getValue() == null ? e.getKey().toString() : e.getKey() + ": " + e.getValue())
				.collect(Collectors.joining(", ", " [", "]"));
	}
	
	@Override
	public void visit(TableGroup tableGroup) {
	
	}
	
	@Override
	public void visit(TablePartial tablePartial) {
	
	}
	
	@Override
	public String toString() {
		return sb.toString();
	}
}
