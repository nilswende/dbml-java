package com.wn.dbml.compiler.parser;

import com.wn.dbml.compiler.Lexer;
import com.wn.dbml.compiler.Parser;
import com.wn.dbml.compiler.ParsingException;
import com.wn.dbml.compiler.Position;
import com.wn.dbml.compiler.token.TokenType;
import com.wn.dbml.model.Column;
import com.wn.dbml.model.ColumnSetting;
import com.wn.dbml.model.Database;
import com.wn.dbml.model.EnumValue;
import com.wn.dbml.model.Index;
import com.wn.dbml.model.IndexSetting;
import com.wn.dbml.model.Name;
import com.wn.dbml.model.NamedNote;
import com.wn.dbml.model.NamedNoteSetting;
import com.wn.dbml.model.Note;
import com.wn.dbml.model.Project;
import com.wn.dbml.model.Relation;
import com.wn.dbml.model.RelationshipSetting;
import com.wn.dbml.model.Schema;
import com.wn.dbml.model.Setting;
import com.wn.dbml.model.SettingHolder;
import com.wn.dbml.model.Table;
import com.wn.dbml.model.TableGroup;
import com.wn.dbml.model.TableGroupSetting;
import com.wn.dbml.model.TableSetting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static com.wn.dbml.compiler.token.TokenType.*;

/**
 * The default parser implementation.
 */
public class ParserImpl implements Parser {
	private List<RelationshipDefinition> relationshipDefinitions;
	private Map<Table, LinkedHashSet<String>> tablePartialRefs;
	private TokenAccess tokenAccess;
	private Database database;
	
	@Override
	public Database parse(Lexer lexer) {
		relationshipDefinitions = new ArrayList<>();
		tablePartialRefs = new HashMap<>();
		tokenAccess = new TokenAccess(lexer);
		database = new Database();
		loop:
		while (true) {
			next(PROJECT, TABLE, REF, ENUM, TABLEGROUP, TABLEPARTIAL, NOTE, EOF);
			switch (tokenType()) {
				case PROJECT -> parseProject();
				case TABLE -> parseTable();
				case REF -> parseRelationship();
				case ENUM -> parseEnum();
				case TABLEGROUP -> parseTableGroup();
				case TABLEPARTIAL -> parseTablePartial();
				case NOTE -> parseNamedNote();
				default -> {
					break loop;
				}
			}
		}
		injectTablePartials();
		createRelationships();
		return database;
	}
	
	private void parseProject() {
		if (database.getProject() != null) {
			error("Project is already defined");
		}
		String name = null;
		next(LITERAL, DSTRING, LBRACE); // projectName
		if (typeIs(LITERAL, DSTRING)) {
			name = tokenValue();
			next(LBRACE);
		}
		var project = new Project(name);
		loop:
		while (true) {
			next(LITERAL, NOTE, RBRACE);
			switch (tokenType()) {
				case LITERAL -> parseProjectProperty(project);
				case NOTE -> project.setNote(parseNote());
				default -> {
					break loop;
				}
			}
		}
		database.setProject(project);
	}
	
	private void parseProjectProperty(Project project) {
		var property = tokenValue();
		next(COLON);
		next(stringTypes());
		project.getProperties().put(property, tokenValue());
	}
	
	private void parseTable() {
		var table = parseTableHead();
		parseTableBody(table);
	}
	
	private Table parseTableHead() {
		var tableName = parseTableName();
		var schema = database.getOrCreateSchema(tableName.schema());
		var table = schema.createTable(tableName.table());
		if (table == null) {
			error("Table '%s' is already defined", tableName);
		}
		parseTableHead(table);
		return table;
	}
	
	private void parseTableHead(Table table) {
		next(AS, LBRACK, LBRACE);
		if (typeIs(AS)) {
			next(LITERAL, DSTRING); // alias
			var alias = tokenValue();
			if (database.containsAlias(alias)) {
				error("Alias '%s' is already defined", alias);
			}
			table.setAlias(alias);
			next(LBRACK, LBRACE);
		}
		if (typeIs(LBRACK)) {
			do {
				next(HEADERCOLOR, NOTE);
				parseTableSetting(table);
				next(COMMA, RBRACK);
			} while (!typeIs(RBRACK));
			next(LBRACE);
		}
	}
	
	private void parseTableSetting(Table table) {
		if (typeIs(HEADERCOLOR)) {
			addSetting(table, TableSetting.HEADERCOLOR, COLOR_CODE);
		} else if (typeIs(NOTE)) {
			table.setNote(parseInlineNote());
		}
	}
	
	private void parseTableBody(Table table) {
		next(LITERAL, DSTRING, TILDE);
		if (typeIs(TILDE)) {
			parseTablePartialRef(table);
		} else {
			parseColumn(table);
		}
		loop:
		while (true) {
			next(LITERAL, DSTRING, TILDE, INDEXES, NOTE, RBRACE);
			switch (tokenType()) {
				case LITERAL, DSTRING -> parseColumn(table);
				case TILDE -> parseTablePartialRef(table);
				case INDEXES -> parseIndexes(table);
				case NOTE -> table.setNote(parseNote());
				default -> {
					break loop;
				}
			}
		}
	}
	
	private void parseTablePartialRef(Table table) {
		next(LITERAL);
		var ref = tokenValue();
		var added = tablePartialRefs.computeIfAbsent(table, x -> new LinkedHashSet<>()).add(ref);
		if (!added) {
			error("Duplicate injection %s", ref);
		}
	}
	
	private void parseColumn(Table table) {
		var name = tokenValue();
		if (table.containsColumn(name)) {
			error("Column '%s' is already defined", Name.of(table, name));
		}
		try (var ignored = new LinebreakMode()) {
			var datatype = parseColumnDatatype();
			var column = table.addColumn(name, datatype);
			if (typeIs(LBRACK)) {
				do {
					next(NOT, NULL, PRIMARY, PK, UNIQUE, INCREMENT, NOTE, REF, DEFAULT);
					parseColumnSetting(column);
					next(COMMA, RBRACK);
				} while (!typeIs(RBRACK));
			}
		}
	}
	
	private String parseColumnDatatype() {
		next(LITERAL, DSTRING); // datatype name
		var datatype = tokenValue();
		if (lookaheadTypeIs(LPAREN)) {
			next(LPAREN);
			var sb = new StringBuilder(datatype);
			sb.append(tokenValue());
			do {
				next(LITERAL, NUMBER, RPAREN, LINEBREAK);
				if (typeIs(LITERAL, NUMBER, RPAREN)) {
					sb.append(tokenValue());
				}
			} while (!typeIs(RPAREN, LINEBREAK));
			datatype = sb.toString();
		}
		next(LBRACK, LINEBREAK);
		return datatype;
	}
	
	private void parseColumnSetting(Column column) {
		switch (tokenType()) {
			case NOT -> {
				next(NULL);
				addSetting(column, ColumnSetting.NOT_NULL);
			}
			case NULL -> {
			} // ignore
			case PRIMARY -> {
				next(KEY);
				addSetting(column, ColumnSetting.PRIMARY_KEY);
			}
			case PK -> addSetting(column, ColumnSetting.PRIMARY_KEY);
			case UNIQUE -> addSetting(column, ColumnSetting.UNIQUE);
			case INCREMENT -> addSetting(column, ColumnSetting.INCREMENT);
			case DEFAULT -> addSetting(column, ColumnSetting.DEFAULT, stringTypesOr(EXPR, BOOLEAN, NUMBER));
			case NOTE -> column.setNote(parseInlineNote());
			case REF -> relationshipDefinitions.add(parseInlineRef(column));
			default -> throw new IllegalStateException("Unexpected value: " + tokenType());
		}
	}
	
	private RelationshipDefinition parseInlineRef(Column columnFrom) {
		String name = null;
		next(LITERAL, DSTRING, COLON); // name
		if (typeIs(LITERAL, DSTRING)) {
			name = tokenValue();
			next(COLON);
		}
		var relation = parseRelation();
		var columnTo = parseColumnName();
		return new RelationshipDefinition(position(),
				name, relation,
				new ColumnNames(columnFrom.getTable().getSchema().getName(), columnFrom.getTable().getName(), List.of(columnFrom.getName())),
				new ColumnNames(columnTo.schema(), columnTo.table(), List.of(columnTo.column())),
				new EnumMap<>(RelationshipSetting.class));
	}
	
	private void parseIndexes(Table table) {
		next(LBRACE);
		do {
			next(LPAREN, LITERAL, EXPR);
			parseIndex(table);
		} while (!lookaheadTypeIs(RBRACE));
		next(RBRACE);
	}
	
	private void parseIndex(Table table) {
		try (var ignored = new LinebreakMode()) {
			var index = table.addIndex();
			if (typeIs(LPAREN)) {
				do {
					next(LITERAL, EXPR);
					parseIndexColumn(table, index);
					next(COMMA, RPAREN);
				} while (!typeIs(RPAREN));
			} else {
				parseIndexColumn(table, index);
			}
			if (lookaheadTypeIs(LBRACK)) {
				next(LBRACK);
				if (lookaheadTypeIs(PK)) {
					next(PK);
					addSetting(index, IndexSetting.PK);
					next(RBRACK);
				} else {
					do {
						next(UNIQUE, NAME, TYPE, NOTE);
						parseIndexSetting(index);
						next(COMMA, RBRACK);
					} while (!typeIs(RBRACK));
				}
			}
			next(LINEBREAK, RBRACE);
		}
	}
	
	private void parseIndexColumn(Table table, Index index) {
		var columnName = tokenValue();
		if (typeIs(LITERAL) && !table.containsColumn(columnName)) {
			error("Column '%s' is not defined", columnName);
		}
		if (!index.addColumn(columnName)) {
			error("Column '%s' is already defined", columnName);
		}
	}
	
	private void parseIndexSetting(Index index) {
		switch (tokenType()) {
			case UNIQUE -> addSetting(index, IndexSetting.UNIQUE);
			case NAME -> addSetting(index, IndexSetting.NAME, stringTypes());
			case TYPE -> addSetting(index, IndexSetting.TYPE, BTREE, HASH);
			case NOTE -> index.setNote(parseInlineNote());
			default -> throw new IllegalStateException("Unexpected value: " + tokenType());
		}
	}
	
	private void parseRelationship() {
		try (var ignored = new LinebreakMode()) {
			boolean linebreak = false;
			String name = null;
			next(LITERAL, DSTRING, LBRACE, COLON, LINEBREAK); // name
			if (typeIs(LINEBREAK)) {
				linebreak = true;
				next(LITERAL, DSTRING, LBRACE, COLON); // name
			}
			if (typeIs(LITERAL, DSTRING)) {
				name = tokenValue();
				next(LBRACE, COLON, LINEBREAK);
				if (typeIs(LINEBREAK)) {
					linebreak = true;
					next(LBRACE);
				}
			}
			if (linebreak && typeIs(COLON)) {
				expected(LBRACE);
			}
			var braced = typeIs(LBRACE);
			if (braced && lookaheadTypeIs(LINEBREAK)) {
				next(LINEBREAK);
			}
			var columnFrom = parseRefColumnNames();
			var relation = parseRelation();
			var columnTo = parseRefColumnNames();
			var settings = parseRelationshipSettings();
			var position = position();
			if (braced) {
				if (lookaheadTypeIs(LINEBREAK)) {
					next(LINEBREAK);
				}
				next(RBRACE);
				position = position();
			} else if (!lookaheadTypeIs(EOF)) {
				next(LINEBREAK);
			}
			var ref = new RelationshipDefinition(position,
					name, relation,
					columnFrom,
					columnTo,
					settings);
			relationshipDefinitions.add(ref);
		}
	}
	
	private Map<RelationshipSetting, String> parseRelationshipSettings() {
		var map = new EnumMap<RelationshipSetting, String>(RelationshipSetting.class);
		if (lookaheadTypeIs(LBRACK)) {
			next(LBRACK);
			do {
				next(DELETE, UPDATE, COLOR);
				var setting = RelationshipSetting.valueOf(tokenType().name());
				String value;
				if (typeIs(COLOR)) {
					next(COLON);
					next(COLOR_CODE);
					value = tokenValue();
				} else {
					next(COLON);
					next(CASCADE, RESTRICT, SET, NO);
					value = tokenValue();
					if (typeIs(SET)) {
						next(NULL, DEFAULT);
						value = multiKeywordValue(value, tokenValue());
					} else if (typeIs(NO)) {
						next(ACTION);
						value = multiKeywordValue(value, tokenValue());
					}
				}
				map.put(setting, value);
				next(COMMA, RBRACK);
			} while (!typeIs(RBRACK));
		}
		return map;
	}
	
	private Relation parseRelation() {
		next(LT, GT, MINUS, NE);
		return Relation.of(tokenValue());
	}
	
	private void parseEnum() {
		var tableName = parseTableName();
		var schema = database.getOrCreateSchema(tableName.schema());
		var anEnum = schema.createEnum(tableName.table());
		if (anEnum == null) {
			error("Enum '%s' is already defined", tableName);
		} else {
			next(LBRACE);
			do {
				next(LITERAL, DSTRING); // name
				var name = tokenValue();
				var enumValue = anEnum.addValue(name);
				if (enumValue == null) {
					error("Enum value '%s' is already defined", Name.of(anEnum, name));
				} else {
					try (var ignored = new LinebreakMode()) {
						next(LBRACK, LINEBREAK);
						if (typeIs(LBRACK)) {
							do {
								next(NOTE);
								parseEnumSetting(enumValue);
								next(COMMA, RBRACK);
							} while (!typeIs(RBRACK));
							next(LINEBREAK);
						}
					}
				}
			} while (!lookaheadTypeIs(RBRACE));
			next(RBRACE);
		}
	}
	
	private void parseEnumSetting(EnumValue value) {
		if (typeIs(NOTE)) {
			value.setNote(parseInlineNote());
		} else {
			throw new IllegalStateException("Unexpected value: " + tokenType());
		}
	}
	
	private void parseTableGroup() {
		var tableGroupName = parseTableName();
		var schema = database.getOrCreateSchema(tableGroupName.schema());
		var tableGroup = schema.createTableGroup(tableGroupName.table());
		if (tableGroup == null) {
			error("TableGroup '%s' is already defined", tableGroupName);
		} else {
			next(LBRACK, LBRACE);
			if (typeIs(LBRACK)) {
				do {
					next(COLOR, NOTE);
					parseTableGroupSetting(tableGroup);
					next(COMMA, RBRACK);
				} while (!typeIs(RBRACK));
				next(LBRACE);
			}
			while (true) {
				if (lookaheadTypeIs(NOTE)) {
					next(NOTE);
					tableGroup.setNote(parseNote());
				} else {
					var tableName = parseTableName();
					var table = findTable(tableName);
					if (!tableGroup.addTable(table)) {
						error("Table '%s' is already defined", table);
					}
				}
				if (lookaheadTypeIs(RBRACE)) {
					next(RBRACE);
					break;
				}
			}
		}
	}
	
	private void parseTableGroupSetting(TableGroup tableGroup) {
		if (typeIs(COLOR)) {
			addSetting(tableGroup, TableGroupSetting.COLOR, COLOR_CODE);
		} else if (typeIs(NOTE)) {
			tableGroup.setNote(parseInlineNote());
		}
	}
	
	private void parseTablePartial() {
		next(LITERAL, DSTRING); // name
		var tableName = tokenValue();
		var partial = database.createTablePartial(tableName);
		if (partial == null) {
			error("TablePartial '%s' is already defined", tableName);
		} else {
			parseTableHead(partial);
			if (partial.getAlias() != null) {
				error("A TablePartial shouldn't have an alias");
			}
			parseTableBody(partial);
		}
	}
	
	private void parseNamedNote() {
		next(LITERAL, DSTRING);
		var noteName = tokenValue();
		var namedNote = database.addNamedNote(noteName);
		if (namedNote == null) {
			error("NamedNote '%s' is already defined", noteName);
		} else {
			next(LBRACK, LBRACE);
			if (typeIs(LBRACK)) {
				do {
					next(HEADERCOLOR);
					parseNamedNoteSetting(namedNote);
					next(COMMA, RBRACK);
				} while (!typeIs(RBRACK));
				next(LBRACE);
			}
			next(stringTypes());
			var value = tokenValue();
			namedNote.setValue(value);
			next(RBRACE);
		}
	}
	
	private void parseNamedNoteSetting(NamedNote namedNote) {
		if (typeIs(HEADERCOLOR)) {
			addSetting(namedNote, NamedNoteSetting.HEADERCOLOR, COLOR_CODE);
		}
	}
	
	private void injectTablePartials() {
		tablePartialRefs.keySet().forEach(this::injectTablePartial);
	}
	
	private void injectTablePartial(Table table) {
		if (!tablePartialRefs.containsKey(table)) {
			return;
		}
		var refList = new ArrayList<>(tablePartialRefs.get(table));
		Collections.reverse(refList);
		for (var ref : refList) { // Java 21: tablePartialRefs.reversed()
			var partial = database.getTablePartial(ref);
			if (!partial.equals(table)) {
				injectTablePartial(partial);
				table.inject(partial);
			}
		}
	}
	
	private void createRelationships() {
		for (var definition : relationshipDefinitions) {
			var from = definition.from();
			var to = definition.to();
			if (from.equals(to)) {
				error(definition, "Two endpoints are the same");
			} else if (from.columns().size() != to.columns().size()) {
				error(definition, "Two endpoints have unequal number of fields");
			}
			var relationship = database.createRelationship(definition.name(), definition.relation(),
					validateColumnNames(definition, from), validateColumnNames(definition, to), definition.settings());
			if (relationship == null) {
				error(definition, "Reference with the same endpoints already exists");
			}
		}
	}
	
	private List<Column> validateColumnNames(RelationshipDefinition definition, ColumnNames names) {
		var schema = database.getSchema(names.schema());
		if (!schema.containsTable(names.table())) {
			error(definition, "Table '%s' is not defined", Name.of(schema, names.table()));
		}
		var table = schema.getTable(names.table());
		for (var column : names.columns()) {
			if (!table.containsColumn(column)) {
				error(definition, "Column '%s' is not defined", Name.of(table, column));
			}
		}
		return names.columns().stream().map(table::getColumn).toList();
	}
	
	private Table findTable(TableName tableName) {
		var table = database.getAlias(tableName.table());
		if (table == null) {
			var tableSchema = database.getSchema(tableName.schema());
			if (tableSchema != null) {
				table = tableSchema.getTable(tableName.table());
			}
		}
		if (table == null) {
			error("Table '%s' is not defined", tableName);
		}
		return table;
	}
	
	private TableName parseTableName() {
		String schemaName = Schema.DEFAULT_NAME, tableName;
		next(LITERAL, DSTRING); // schemaName, tableName
		tableName = tokenValue();
		if (lookaheadTypeIs(DOT)) {
			next(DOT);
			next(LITERAL, DSTRING); // tableName
			schemaName = tableName;
			tableName = tokenValue();
		}
		return new TableName(schemaName, tableName);
	}
	
	private ColumnName parseColumnName() {
		String schemaName = Schema.DEFAULT_NAME, tableName, columnName;
		next(LITERAL, DSTRING); // schemaName, tableName
		tableName = tokenValue();
		next(DOT);
		next(LITERAL, DSTRING); // tableName, columnName
		columnName = tokenValue();
		if (lookaheadTypeIs(DOT)) {
			next(DOT);
			next(LITERAL, DSTRING); // columnName
			schemaName = tableName;
			tableName = columnName;
			columnName = tokenValue();
		}
		return new ColumnName(schemaName, tableName, columnName);
	}
	
	private ColumnNames parseRefColumnNames(TokenType... after) {
		String schemaName = Schema.DEFAULT_NAME, tableName, columnName;
		var columnNames = new ArrayList<String>();
		next(LITERAL, DSTRING); // schemaName, tableName
		tableName = tokenValue();
		next(DOT);
		next(LITERAL, DSTRING, LPAREN); // tableName, columnName
		if (typeIs(LITERAL, DSTRING)) {
			columnName = tokenValue();
			if (lookaheadTypeIs(DOT)) {
				next(DOT);
				schemaName = tableName;
				tableName = columnName;
				next(LITERAL, DSTRING, LPAREN); // columnName
				if (typeIs(LITERAL, DSTRING)) {
					columnNames.add(tokenValue());
				} else if (typeIs(LPAREN)) {
					parseRefColumnNames(columnNames);
				}
			} else {
				columnNames.add(columnName);
			}
		} else if (typeIs(LPAREN)) {
			parseRefColumnNames(columnNames);
		}
		next(after);
		return new ColumnNames(schemaName, tableName, columnNames);
	}
	
	private void parseRefColumnNames(List<String> columnNames) {
		do {
			next(LITERAL, DSTRING); // columnName
			columnNames.add(tokenValue());
			next(COMMA, RPAREN);
		} while (!typeIs(RPAREN));
	}
	
	private Note parseNote() {
		next(COLON, LBRACE);
		var braced = typeIs(LBRACE);
		next(stringTypes());
		var note = tokenValue();
		if (braced) {
			next(RBRACE);
		}
		return new Note(note);
	}
	
	private Note parseInlineNote() {
		next(COLON);
		next(stringTypes());
		return new Note(tokenValue());
	}
	
	private <T extends Setting> void addSetting(SettingHolder<T> holder, T setting, TokenType... types) {
		if (types != null && types.length > 0) {
			next(COLON);
			next(types);
		}
		holder.addSetting(setting, tokenValue());
	}
	
	private TokenType[] stringTypes() {
		return new TokenType[]{SSTRING, DSTRING, TSTRING};
	}
	
	private TokenType[] stringTypesOr(TokenType... types) {
		return types != null && types.length > 0 ? concat(stringTypes(), types) : stringTypes();
	}
	
	private String multiKeywordValue(String... keywords) {
		return String.join(MULTI_SEPARATOR, keywords);
	}
	
	private static <T> T[] concat(T[] first, T[] second) {
		var result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}
	
	private void next(TokenType... types) {
		tokenAccess.next(types);
	}
	
	private TokenType tokenType() {
		return tokenAccess.type();
	}
	
	private String tokenValue() {
		return tokenAccess.value();
	}
	
	private boolean lookaheadTypeIs(TokenType type) {
		return tokenAccess.lookaheadTypeIs(type);
	}
	
	private boolean typeIs(TokenType type) {
		return tokenAccess.typeIs(type);
	}
	
	private boolean typeIs(TokenType... types) {
		return tokenAccess.typeIs(types);
	}
	
	private void expected(TokenType... types) {
		tokenAccess.expected(types);
	}
	
	private void error(RelationshipDefinition definition, String msg, Object... args) {
		error(definition, String.format(msg, args));
	}
	
	private void error(RelationshipDefinition definition, String msg) {
		throw new ParsingException(definition.position(), msg);
	}
	
	private void error(String msg, Object... args) {
		tokenAccess.error(msg, args);
	}
	
	private Position position() {
		return tokenAccess.position();
	}
	
	@Override
	public String toString() {
		return tokenAccess.toString();
	}
	
	private class LinebreakMode implements AutoCloseable {
		public LinebreakMode() {
			tokenAccess.setIgnoreLinebreaks(false);
		}
		
		@Override
		public void close() {
			tokenAccess.setIgnoreLinebreaks(true);
		}
	}
	
	private class SpaceMode implements AutoCloseable {
		public SpaceMode() {
			tokenAccess.setIgnoreSpaces(false);
		}
		
		@Override
		public void close() {
			tokenAccess.setIgnoreSpaces(true);
		}
	}
	
	private record TableName(
			String schema, String table
	) {
		@Override
		public String toString() {
			return Name.ofTable(schema, table);
		}
	}
	
	private record ColumnName(
			String schema, String table, String column
	) {
		@Override
		public String toString() {
			return Name.ofColumn(schema, table, column);
		}
	}
	
	private record ColumnNames(
			String schema, String table, List<String> columns
	) {
		@Override
		public String toString() {
			return Name.ofColumns(schema, table, columns);
		}
	}
	
	private record RelationshipDefinition(
			Position position,
			String name, Relation relation,
			ColumnNames from, ColumnNames to,
			Map<RelationshipSetting, String> settings
	) {
	}
}
