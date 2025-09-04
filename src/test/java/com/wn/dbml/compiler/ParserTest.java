package com.wn.dbml.compiler;

import com.wn.dbml.compiler.lexer.LexerImpl;
import com.wn.dbml.compiler.parser.ParserImpl;
import com.wn.dbml.model.Column;
import com.wn.dbml.model.ColumnSetting;
import com.wn.dbml.model.Database;
import com.wn.dbml.model.IndexSetting;
import com.wn.dbml.model.Name;
import com.wn.dbml.model.NamedNoteSetting;
import com.wn.dbml.model.RelationshipSetting;
import com.wn.dbml.model.Schema;
import com.wn.dbml.model.TableSetting;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
	
	private Database parse(String dbml) {
		return new ParserImpl().parse(new LexerImpl(dbml));
	}
	
	private Schema getDefaultSchema(Database database) {
		return database.getSchema(Schema.DEFAULT_NAME);
	}
	
	@Test
	void testParseProject() {
		var dbml = """
				Project project_name {
				  database_type: 'PostgreSQL'
				  Note: 'Description of the project'
				}""";
		var database = parse(dbml);
		
		assertNotNull(database.getProject());
		var project = database.getProject();
		assertEquals("project_name", project.getName());
		var properties = project.getProperties();
		assertEquals(1, properties.size());
		assertTrue(properties.containsKey("database_type"));
		assertEquals("PostgreSQL", properties.get("database_type"));
		assertNotNull(project.getNote());
		assertEquals("Description of the project", project.getNote().getValue());
	}
	
	@Test
	void testParseProjectWithOthers() {
		var dbml = """
				Project project_name {
				}
				
				Table t1 {
				  id integer
				}
				Table t2 {
				  id integer
				}
				
				Ref: t1.id - t2.id
				
				TableGroup tbls {
				  t1
				  t2
				}
				
				Enum e {
				  val1
				  val2
				  val3
				}""";
		var database = parse(dbml);
		
		assertNotNull(database.getProject());
		var project = database.getProject();
		assertEquals("project_name", project.getName());
		var schema = database.getSchema(Schema.DEFAULT_NAME);
		assertNotNull(schema);
		assertTrue(schema.containsTable("t1"));
		assertTrue(schema.containsTable("t2"));
		assertEquals(1, database.getRelationships().size());
		assertEquals(1, database.getTableGroups().size());
		assertEquals(1, schema.getEnums().size());
	}
	
	@Test
	void testParseProjectLongForm() {
		var dbml = """
				Project project_name {
				  Table t1 {
				    id integer
				  }
				  Table t2 {
				    id integer
				  }
				
				  Ref: t1.id - t2.id
				
				  TableGroup tbls {
				    t1
				    t2
				  }
				
				  Enum e {
				    val1
				    val2
				    val3
				  }
				}""";
		
		// was valid once, but not anymore
		assertThrows(ParsingException.class, () -> parse(dbml));
	}
	
	@Test
	void testParseProjectExisting() {
		var dbml = """
				Project project_name {
				  database_type: 'PostgreSQL'
				  Note: 'Description of the project'
				}
				
				Project project_name2 {
				  database_type: 'PostgreSQL'
				  Note: 'Description of the project'
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertEquals("[6:7] Project is already defined", e.getMessage());
	}
	
	@Test
	void testParseTwice() {
		var dbml = """
				Table schema1.table1 {
				  id integer
				  column1 integer
				}
				
				Table schema2.table2 {
				  id integer
				  column2 integer
				}
				
				Ref r1: schema2.table2.column2 - schema1.table1.column1""";
		var parser = new ParserImpl();
		
		var database = parser.parse(new LexerImpl(dbml));
		var relationships = database.getRelationships();
		assertEquals(1, relationships.size());
		
		database = parser.parse(new LexerImpl(dbml));
		relationships = database.getRelationships();
		assertEquals(1, relationships.size());
	}
	
	@Test
	void testParseSchemaEmpty() {
		var dbml = """
				Table .users {
				  id integer
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertTrue(e.getMessage().startsWith("[1:12] unexpected token 'LITERAL'"));
	}
	
	@Test
	void testParseNoteEmpty() {
		var dbml = """
				Table users {
				  id integer [note: '']
				  note: ""
				}""";
		var database = parse(dbml);
		
		var schema = getDefaultSchema(database);
		var users = schema.getTable("users");
		assertNotNull(users.getNote());
		assertEquals("", users.getNote().getValue());
		var idColumn = users.getColumn("id");
		assertNotNull(idColumn);
		assertNotNull(idColumn.getNote());
		assertEquals("", idColumn.getNote().getValue());
	}
	
	@Test
	void testParseSchemaEmptyString() {
		var dbml = """
				Table "".users {
				  id integer
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertEquals("[1:14] Schema must have a name", e.getMessage());
	}
	
	@Test
	void testParseColumnEmpty() {
		var dbml = """
				Table users {
				  "" integer
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertEquals("[2:13] Column must have a name", e.getMessage());
	}
	
	@Test
	void testParseEnumValueEmpty() {
		var dbml = """
				enum v1.job_status {
				  ""
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertEquals("[2:4] Enum value must have a name", e.getMessage());
	}
	
	@Test
	void testParseNamedNoteValueEmpty() {
		var dbml = """
				Note "" {
				  ''
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertEquals("[1:7] NamedNote must have a name", e.getMessage());
	}
	
	@Test
	void testParseTablePartialValueEmpty() {
		var dbml = """
				TablePartial "" {
				  id int [pk, not null]
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertEquals("[1:15] TablePartial must have a name", e.getMessage());
	}
	
	@Test
	void testParseTable() {
		var dbml = """
				Table s.users as U [headercolor: #3498DB] {
				  id integer [primary key, increment, note: 'replace text here']
				  username "varchar(255)" [not null, unique, default: "null"]
				  Note {
				    'Stores user data'
				  }
				}""";
		var database = parse(dbml);
		
		assertEquals(1, database.getSchemas().size());
		var schema = database.getSchema("s");
		assertNotNull(schema);
		assertEquals(1, schema.getTables().size());
		var table = schema.getTable("users");
		assertNotNull(table);
		assertEquals("U", table.getAlias());
		var tableSettings = table.getSettings();
		assertEquals(1, tableSettings.size());
		assertTrue(tableSettings.containsKey(TableSetting.HEADERCOLOR));
		assertEquals("3498DB", tableSettings.get(TableSetting.HEADERCOLOR));
		assertEquals(2, table.getColumns().size());
		var id = table.getColumn("id");
		assertEquals("id", id.getName());
		assertEquals("integer", id.getType());
		var idSettings = id.getSettings();
		assertEquals(2, idSettings.size());
		assertTrue(idSettings.containsKey(ColumnSetting.PRIMARY_KEY));
		assertTrue(idSettings.containsKey(ColumnSetting.INCREMENT));
		assertNotNull(id.getNote());
		assertEquals("replace text here", id.getNote().getValue());
		var username = table.getColumn("username");
		assertEquals("username", username.getName());
		assertEquals("varchar(255)", username.getType());
		var usernameSettings = username.getSettings();
		assertEquals(3, usernameSettings.size());
		assertTrue(usernameSettings.containsKey(ColumnSetting.NOT_NULL));
		assertTrue(usernameSettings.containsKey(ColumnSetting.UNIQUE));
		assertEquals("null", usernameSettings.get(ColumnSetting.DEFAULT));
		assertNotNull(table.getNote());
		assertEquals("Stores user data", table.getNote().getValue());
	}
	
	@Test
	void testParseTableHeaderNote() {
		var dbml = """
				Table table1 [note: 'Note'] {
				  id integer
				}""";
		var database = parse(dbml);
		
		var schema = getDefaultSchema(database);
		var table = schema.getTable("table1");
		assertNotNull(table.getNote());
		assertEquals("Note", table.getNote().getValue());
	}
	
	@Test
	void testParseTableSettings() {
		var dbml = """
				Table users [note: 'User data'] {
				  id int [pk, increment, note: 'Primary key']
				}""";
		var database = parse(dbml);
		
		var schema = getDefaultSchema(database);
		var users = schema.getTable("users");
		assertNotNull(users);
		var usersNote = users.getNote();
		assertNotNull(usersNote);
		assertEquals("User data", usersNote.getValue());
		var id = users.getColumn("id");
		assertNotNull(id);
		assertNotNull(id.getSettings().get(ColumnSetting.PRIMARY_KEY));
		assertNotNull(id.getSettings().get(ColumnSetting.INCREMENT));
		var idNote = id.getNote();
		assertNotNull(idNote);
		assertEquals("Primary key", idNote.getValue());
	}
	
	@Test
	void testParseComplexColumnDatatype() {
		var dbml = """
				Table users {
				  id char(8)
				  username varchar(255) [not null, unique]
				  Note: 'Stores user data'
				}""";
		var database = parse(dbml);
		
		var schema = getDefaultSchema(database);
		var users = schema.getTable("users");
		assertNotNull(users);
		var id = users.getColumn("id");
		assertNotNull(id);
		assertEquals("char(8)", id.getType());
		var username = users.getColumn("username");
		assertNotNull(username);
		assertEquals("varchar(255)", username.getType());
		assertNotNull(username.getSettings().get(ColumnSetting.NOT_NULL));
		assertNotNull(username.getSettings().get(ColumnSetting.UNIQUE));
		var usersNote = users.getNote();
		assertNotNull(usersNote);
		assertEquals("Stores user data", usersNote.getValue());
	}
	
	@Test
	void testExtraWordColumnDefFails() {
		var dbml = """
				Table table1 {
				  id int integer
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertEquals(2, e.getPosition().line());
		assertEquals(16, e.getPosition().column());
	}
	
	@Test
	void testParseTableExisting() {
		var dbml = """
				Table table1 {
				  id integer
				}
				
				Table table1 {
				  id integer
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertEquals("[5:12] Table 'table1' is already defined", e.getMessage());
	}
	
	@Test
	void testParseTableAliasExisting() {
		var dbml = """
				Table table1 as T {
				  id integer
				}
				
				Table table2 as T {
				  id integer
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertEquals("[5:17] Alias 'T' is already defined", e.getMessage());
	}
	
	@Test
	void testParseTableColumnExisting() {
		var dbml = """
				Table table1 {
				  id integer
				  id integer2
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertEquals("[3:4] Column 'table1.id' is already defined", e.getMessage());
	}
	
	@Test
	void testColumnDatatypeLiteralArgs() {
		var dbml = """
				Table bill_of_materials {
				  quantity DECIMAL(10, 2)
				}""";
		var database = parse(dbml);
		
		var schema = getDefaultSchema(database);
		var table = schema.getTable("bill_of_materials");
		assertNotNull(table);
		var quantity = table.getColumn("quantity");
		assertNotNull(quantity);
		assertEquals("DECIMAL(10,2)", quantity.getType());
	}
	
	@Test
	void testColumnDatatypeDStringArgs() {
		var dbml = """
				Table bill_of_materials {
				  quantity "DECIMAL(10,2)"
				}""";
		var database = parse(dbml);
		
		var schema = getDefaultSchema(database);
		var table = schema.getTable("bill_of_materials");
		assertNotNull(table);
		var quantity = table.getColumn("quantity");
		assertNotNull(quantity);
		assertEquals("DECIMAL(10,2)", quantity.getType());
	}
	
	@Test
	void testColumnDatatypeArgsWithSetting() {
		var dbml = """
				Table bill_of_materials {
				  quantity DECIMAL(10, 2) [not null]
				  unit VARCHAR(32)
				}""";
		var database = parse(dbml);
		
		var schema = getDefaultSchema(database);
		var table = schema.getTable("bill_of_materials");
		assertNotNull(table);
		var quantity = table.getColumn("quantity");
		assertNotNull(quantity);
		assertEquals("DECIMAL(10,2)", quantity.getType());
		var setting = quantity.getSettings().get(ColumnSetting.NOT_NULL);
		assertNotNull(setting);
		var unit = table.getColumn("unit");
		assertNotNull(unit);
		
	}
	
	@Test
	void testParseIndexes() {
		var dbml = """
				Table bookings {
				  id integer
				  country varchar
				  booking_date date
				  created_at timestamp
				
				  indexes {
				    (id, country) [pk] // composite primary key
				
				    created_at [name: 'created_at_index', note: 'Date']
				    booking_date
				    (country, booking_date) [unique]
				    booking_date [type: hash]
				
				    (`id*2`)
				    (`id*3`,`getdate()`)
				    (`id*3`,id)
				  }
				}""";
		var database = parse(dbml);
		
		var schema = getDefaultSchema(database);
		var table = schema.getTable("bookings");
		assertNotNull(table);
		var indexes = table.getIndexes();
		assertNotNull(indexes);
		assertEquals(8, indexes.size());
		var index = table.getIndex("created_at_index");
		assertNotNull(index);
	}
	
	@Test
	void testParseIndexesColumnMissing() {
		var dbml = """
				Table table1 {
				  id integer
				
				  indexes {
				    (name)
				  }
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertEquals("[5:9] Column 'name' is not defined", e.getMessage());
	}
	
	@Test
	void testParseIndexesColumnExisting() {
		var dbml = """
				Table table1 {
				  id integer
				
				  indexes {
				    (id, id)
				  }
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertEquals("[5:11] Column 'id' is already defined", e.getMessage());
	}
	
	@Test
	void testParseEnum() {
		var dbml = """
				enum v1.job_status {
				  created [note: 'Waiting to be processed']
				  running
				  done
				  FAILURE
				 }""";
		var database = parse(dbml);
		
		var schema = database.getSchema("v1");
		assertNotNull(schema);
		var enumName = "job_status";
		var anEnum = schema.getEnum(enumName);
		assertNotNull(anEnum);
		assertTrue(schema.containsEnum(enumName));
		var enumValues = anEnum.getValues();
		assertEquals(4, enumValues.size());
		var firstValue = enumValues.iterator().next();
		assertEquals("Waiting to be processed", firstValue.getNote().getValue());
	}
	
	@Test
	void testParseEnumExisting() {
		var dbml = """
				enum e {
				  v1
				  v2
				}
				
				enum e {
				  v1
				  v2
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertEquals("[6:6] Enum 'e' is already defined", e.getMessage());
	}
	
	@Test
	void testParseEnumValueExisting() {
		var dbml = """
				enum e {
				  v1
				  v1
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertEquals("[3:4] Enum value 'e.v1' is already defined", e.getMessage());
	}
	
	@Test
	void testParseTableGroup() {
		var dbml = """
				Table table1 {
				  id integer
				}
				Table table2 {
				  id integer
				}
				Table table3 as C {
				  id integer
				}
				
				TableGroup tablegroup_name [color: #fff, note: "group note attr"] {
				  table1
				  table2
				  C
				  note: 'group note element'
				}""";
		var database = parse(dbml);
		
		var tableGroupName = "tablegroup_name";
		var tableGroup = database.getTableGroup(tableGroupName);
		assertNotNull(tableGroup);
		assertTrue(database.containsTableGroup(tableGroupName));
		var tables = tableGroup.getTables();
		assertEquals(3, tables.size());
		var note = tableGroup.getNote();
		assertNotNull(note);
		assertEquals("group note element", note.getValue());
	}
	
	@Test
	void testParseTableGroupExisting() {
		var dbml = """
				Table table1 {
				  id integer
				}
				Table table2 {
				  id integer
				}
				
				TableGroup tg {
				  table1
				  table2
				}
				
				TableGroup tg {
				  table1
				  table2
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertEquals("[13:13] TableGroup 'tg' is already defined", e.getMessage());
	}
	
	@Test
	void testParseTableGroupTableMissing() {
		var dbml = """
				Table table1 {
				  id integer
				}
				
				TableGroup tg {
				  table1
				  table2
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertEquals("[7:8] Table 'table2' is not defined", e.getMessage());
	}
	
	@Test
	void testParseTableGroupTableExisting() {
		var dbml = """
				Table table1 {
				  id integer
				}
				
				TableGroup tg {
				  table1
				  table1
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertEquals("[7:8] Table 'table1' is already defined", e.getMessage());
	}
	
	@Test
	void testParseTableGroupSchema() {
		var dbml = """
				Table table1 {
				  id integer
				}
				
				TableGroup a.tg {
				  table1
				}""";
		
		// was valid once, but not anymore
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertTrue(e.getMessage().startsWith("[5:13] unexpected token 'DOT'"));
	}
	
	@Test
	void testParseRefInline() {
		var dbml = """
				Table schema1.table1 {
				  id integer
				  column1 integer
				}
				
				Table schema2.table2 {
				  id integer [ref name: < schema1.table1.id]
				  column2 integer [ref: > schema1.table1.column1]
				}""";
		var database = parse(dbml);
		
		var relationships = database.getRelationships();
		assertEquals(2, relationships.size());
		var iterator = relationships.iterator();
		var ref = iterator.next();
		assertEquals("name", ref.getName());
		assertEquals("<", ref.getRelation().getSymbol());
		validateRefColumn(ref.getFrom(), 1, "schema2.table2.id");
		validateRefColumn(ref.getTo(), 1, "schema1.table1.id");
		ref = iterator.next();
		assertEquals(">", ref.getRelation().getSymbol());
		validateRefColumn(ref.getFrom(), 1, "schema2.table2.column2");
		validateRefColumn(ref.getTo(), 1, "schema1.table1.column1");
	}
	
	private void validateRefColumn(List<Column> columns, int num, String name) {
		assertEquals(num, columns.size());
		var column = columns.iterator().next();
		if (columns.size() == 1) {
			assertEquals(name, column.toString());
		} else {
			var names = columns.stream().map(Column::getName).toList();
			var schemaName = column.getTable().getSchema().getName();
			var tableName = column.getTable().getName();
			var columnsName = Name.ofColumns(schemaName, tableName, names);
			assertEquals(name, columnsName);
		}
	}
	
	@Test
	void testParseRefSchemaSingleColumn() {
		var dbml = """
				Table schema1.table1 {
				  id integer
				  column1 integer
				}
				
				Table schema2.table2 {
				  id integer
				  column2 integer
				}
				
				Ref r1: schema2.table2.column2 - schema1.table1.column1""";
		var database = parse(dbml);
		
		var relationships = database.getRelationships();
		assertEquals(1, relationships.size());
		var refName = "r1";
		var ref = database.getRelationship(refName);
		assertNotNull(ref);
		assertTrue(database.containsRelationship(refName));
		assertEquals("-", ref.getRelation().getSymbol());
		validateRefColumn(ref.getFrom(), 1, "schema2.table2.column2");
		validateRefColumn(ref.getTo(), 1, "schema1.table1.column1");
	}
	
	@Test
	void testParseRefSchemaMultiColumn() {
		var dbml = """
				Table schema1.table1 {
				  id integer
				  column1 integer
				}
				
				Table schema2.table2 {
				  id integer
				  column2 integer
				}
				
				Ref r1: schema2.table2.(id, column2) <> schema1.table1.(id, column1)""";
		var database = parse(dbml);
		
		var relationships = database.getRelationships();
		assertEquals(1, relationships.size());
		var refName = "r1";
		var ref = database.getRelationship(refName);
		assertNotNull(ref);
		assertTrue(database.containsRelationship(refName));
		assertEquals("<>", ref.getRelation().getSymbol());
		validateRefColumn(ref.getFrom(), 2, "schema2.table2.(id, column2)");
		validateRefColumn(ref.getTo(), 2, "schema1.table1.(id, column1)");
	}
	
	@Test
	void testParseRefColumn() {
		var dbml = """
				Table table1 {
				  id integer
				  column1 integer
				}
				
				Table table2 {
				  id integer
				  column2 integer
				}
				
				Ref r1: table2.column2 <> table1.column1 [delete: cascade, update: no   action, color: #79AD51]""";
		var database = parse(dbml);
		
		var relationships = database.getRelationships();
		assertEquals(1, relationships.size());
		var refName = "r1";
		var ref = database.getRelationship(refName);
		assertNotNull(ref);
		assertTrue(database.containsRelationship(refName));
		assertEquals("<>", ref.getRelation().getSymbol());
		validateRefColumn(ref.getFrom(), 1, "table2.column2");
		validateRefColumn(ref.getTo(), 1, "table1.column1");
		assertEquals("cascade", ref.getSettings().get(RelationshipSetting.DELETE));
		assertEquals("no action", ref.getSettings().get(RelationshipSetting.UPDATE));
		assertEquals("79AD51", ref.getSettings().get(RelationshipSetting.COLOR));
	}
	
	@Test
	void testParseRefMultiColumn() {
		var dbml = """
				Table table1 {
				  id integer
				  column1 integer
				}
				
				Table table2 {
				  id integer
				  column2 integer
				}
				
				Ref r1: table2.(id, column2) <> table1.(id, column1) [delete: set null]""";
		var database = parse(dbml);
		
		var relationships = database.getRelationships();
		assertEquals(1, relationships.size());
		var refName = "r1";
		var ref = database.getRelationship(refName);
		assertNotNull(ref);
		assertTrue(database.containsRelationship(refName));
		assertEquals("<>", ref.getRelation().getSymbol());
		validateRefColumn(ref.getFrom(), 2, "table2.(id, column2)");
		validateRefColumn(ref.getTo(), 2, "table1.(id, column1)");
		assertEquals("set null", ref.getSettings().get(RelationshipSetting.DELETE));
	}
	
	@Test
	void testParseRefSettings() {
		var dbml = """
				Table table1 {
				  id integer
				  column1 integer
				}
				
				Table table2 {
				  id integer
				  column2 integer
				}
				
				Ref {
				  table2.column2 < table1.column1 [update: set null, delete: cascade, color: #aabbcc]
				}""";
		var database = parse(dbml);
		
		var relationships = database.getRelationships();
		assertEquals(1, relationships.size());
		var ref = relationships.iterator().next();
		assertEquals("<", ref.getRelation().getSymbol());
		validateRefColumn(ref.getFrom(), 1, "table2.column2");
		validateRefColumn(ref.getTo(), 1, "table1.column1");
		var settings = ref.getSettings();
		assertEquals(3, settings.size());
		var update = settings.get(RelationshipSetting.UPDATE);
		assertEquals("set null", update);
		var delete = settings.get(RelationshipSetting.DELETE);
		assertEquals("cascade", delete);
		var color = settings.get(RelationshipSetting.COLOR);
		assertEquals("aabbcc", color);
	}
	
	@Test
	void testParseRefSameColumns() {
		var dbml = """
				Table table1 {
				  id integer
				}
				Table table2 {
				  id integer
				}
				
				Ref: table1.id - table1.id
				""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertEquals("[8:26] Two endpoints are the same", e.getMessage());
	}
	
	@Test
	void testParseRefUnequalSize() {
		var dbml = """
				Table table1 {
				  id integer
				}
				Table table2 {
				  id integer
				  name string
				}
				
				Ref: table1.id - table2.(id, name)
				""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertEquals("[9:34] Two endpoints have unequal number of fields", e.getMessage());
	}
	
	@Test
	void testParseRefMissingTable() {
		var dbml = """
				Table table1 {
				  id integer
				}
				Table table2 {
				  id integer
				}
				
				Ref: table1.id - table3.id
				""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertEquals("[8:26] Table 'table3' is not defined", e.getMessage());
	}
	
	@Test
	void testParseRefMissingColumn() {
		var dbml = """
				Table table1 {
				  id integer
				}
				Table table2 {
				  id integer
				}
				
				Ref: table1.id - table2.name
				""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertEquals("[8:28] Column 'table2.name' is not defined", e.getMessage());
	}
	
	@Test
	void testParseRefExisting() {
		var dbml = """
				Table table1 {
				  id integer
				}
				Table table2 {
				  id integer
				}
				
				Ref: table1.id - table2.id
				Ref
				{
				  table1.id - table2.id
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertEquals("[12:1] Reference with the same endpoints already exists", e.getMessage());
	}
	
	@Test
	void testParseNamedRefShortFormLinebreakBeforeColon() {
		var dbml = """
				Table table1 {
				  id integer
				}
				Table table2 {
				  id integer
				}
				
				Ref r1
				  : table1.id - table2.id""";
		
		assertThrows(ParsingException.class, () -> parse(dbml));
	}
	
	@Test
	void testParseRefShortFormLinebreakBeforeColon() {
		var dbml = """
				Table table1 {
				  id integer
				}
				Table table2 {
				  id integer
				}
				
				Ref
				  : table1.id - table2.id""";
		
		assertThrows(ParsingException.class, () -> parse(dbml));
	}
	
	@Test
	void testParseRefShortFormLinebreakAfterColon() {
		var dbml = """
				Table table1 {
				  id integer
				}
				Table table2 {
				  id integer
				}
				
				Ref:
				  table1.id - table2.id""";
		
		assertThrows(ParsingException.class, () -> parse(dbml));
	}
	
	@Test
	void testKeywordLiteralSubstitution() {
		var dbml = """
				Table table1 {
				  table integer
				}""";
		var database = parse(dbml);
		
		var schema = getDefaultSchema(database);
		var table = schema.getTable("table1");
		var column = table.getColumn("table");
		assertNotNull(column);
		assertEquals("integer", column.getType());
	}
	
	@Test
	void testMultiKeywordAsColumnDef() {
		var dbml = """
				Table table1 {
				  not null
				}""";
		var database = parse(dbml);
		
		var schema = getDefaultSchema(database);
		var table = schema.getTable("table1");
		var not = table.getColumn("not");
		assertNotNull(not);
		assertEquals("null", not.getType());
	}
	
	@Test
	void testMultiKeywordWhitespace() {
		var dbml = """
				Table table1 {
				  id integer [not   null]
				  name varchar [not\tnull]
				}""";
		var database = parse(dbml);
		
		var schema = getDefaultSchema(database);
		var table = schema.getTable("table1");
		assertNotNull(table);
		var id = table.getColumn("id");
		assertNotNull(id);
		assertNotNull(id.getSettings().get(ColumnSetting.NOT_NULL));
		var name = table.getColumn("name");
		assertNotNull(name);
		assertNotNull(name.getSettings().get(ColumnSetting.NOT_NULL));
	}
	
	@Test
	void testParseNotMultiKeyword() {
		var dbml = """
				Table table1 {
				  not integer
				}""";
		var database = parse(dbml);
		
		var schema = getDefaultSchema(database);
		var table = schema.getTable("table1");
		assertEquals("integer", table.getColumn("not").getType());
	}
	
	@Test
	void testParseColumnDefault() {
		var dbml = """
				Table Organization {
				  organization_id integer [pk]
				  name varchar [not null]
				  code varchar [unique, not null]
				  created_at timestamp [default: `CURRENT_TIMESTAMP`]
				  updated_at timestamp [default: `CURRENT_TIMESTAMP`]
				  active boolean [default: true]
				  percent decimal [default: 100.0]
				  def1 decimal [default: 1.]
				  def2 decimal [default: -1]
				  def3 decimal [default: - 1]
				  def4 decimal [default: - 1.]
				  def5 decimal [default: - 1.2]
				  number int [default: 0]
				}""";
		var database = parse(dbml);
		
		var schema = getDefaultSchema(database);
		var table = schema.getTable("Organization");
		assertEquals("CURRENT_TIMESTAMP", table.getColumn("created_at").getSettings().get(ColumnSetting.DEFAULT));
		assertEquals("CURRENT_TIMESTAMP", table.getColumn("updated_at").getSettings().get(ColumnSetting.DEFAULT));
		assertEquals("true", table.getColumn("active").getSettings().get(ColumnSetting.DEFAULT));
		assertEquals("100.0", table.getColumn("percent").getSettings().get(ColumnSetting.DEFAULT));
		assertEquals("1", table.getColumn("def1").getSettings().get(ColumnSetting.DEFAULT));
		assertEquals("-1", table.getColumn("def2").getSettings().get(ColumnSetting.DEFAULT));
		assertEquals("-1", table.getColumn("def3").getSettings().get(ColumnSetting.DEFAULT));
		assertEquals("-1", table.getColumn("def4").getSettings().get(ColumnSetting.DEFAULT));
		assertEquals("-1.2", table.getColumn("def5").getSettings().get(ColumnSetting.DEFAULT));
		assertEquals("0", table.getColumn("number").getSettings().get(ColumnSetting.DEFAULT));
	}
	
	@Test
	void testParseColumnDefaultInvalidLiteral() {
		var dbml = """
				Table Organization {
				  organization_id integer [pk]
				  number int [default: invalid]
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertTrue(e.getMessage().startsWith("[3:30] unexpected token 'LITERAL'"), e.getMessage());
	}
	
	@Test
	void testParseColumnDefaultInvalidDecimalLiteral() {
		var dbml = """
				Table Organization {
				  organization_id integer [pk]
				  number int [default: 1.invalid]
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertTrue(e.getMessage().startsWith("[3:32] unexpected token 'LITERAL'"), e.getMessage());
	}
	
	@Test
	void testParseColumnDefaultInvalidDecimalPrefix() {
		var dbml = """
				Table Organization {
				  organization_id integer [pk]
				  number int [default: .1]
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertTrue(e.getMessage().startsWith("[3:24] unexpected token 'LITERAL'"), e.getMessage());
	}
	
	@Test
	void testParseColumnDefaultInvalidDecimalPointSpaces() {
		var dbml = """
				Table Organization {
				  organization_id integer [pk]
				  number int [default: 1 . 2]
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertTrue(e.getMessage().startsWith("[3:26] unexpected token 'DOT'"), e.getMessage());
	}
	
	@Test
	void testUmlaut() {
		var dbml = """
				Table Üser {
					name varchar
				}""";
		var database = parse(dbml);
		
		var schema = getDefaultSchema(database);
		var table = schema.getTable("Üser");
		assertNotNull(table);
	}
	
	@Test
	void testNumberTableName() {
		var dbml = """
				Table 2 {
				  organization_id integer [pk]
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertTrue(e.getMessage().startsWith("[1:7] unexpected token 'NUMBER'"), e.getMessage());
	}
	
	@Test
	void testNumberDecimalTableName() {
		var dbml = """
				Table 1.2 {
				  organization_id integer [pk]
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertTrue(e.getMessage().startsWith("[1:9] unexpected token 'NUMBER'"), e.getMessage());
	}
	
	@Test
	void testNumberSchemaName() {
		var dbml = """
				Table 2.Organization {
				  organization_id integer [pk]
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertTrue(e.getMessage().startsWith("[1:8] unexpected token 'NUMBER'"), e.getMessage());
	}
	
	@Test
	void testNamedNote() {
		var dbml = """
				Table table1 {
				  table integer
				}
				
				Note single_line_note {
				  'This is a single line note'
				}
				
				Note multiple_lines_note [headercolor: #fff] {
				'''
				  This is a multiple lines note
				  This string can spans over multiple lines.
				'''
				}""";
		var database = parse(dbml);
		
		var namedNotes = database.getNamedNotes();
		assertEquals(2, namedNotes.size());
		var singleLineNote = database.getNamedNote("single_line_note");
		assertNotNull(singleLineNote);
		assertEquals("This is a single line note", singleLineNote.getValue());
		assertNull(singleLineNote.getSettings().get(NamedNoteSetting.HEADERCOLOR));
		var multipleLinesNote = database.getNamedNote("multiple_lines_note");
		assertNotNull(multipleLinesNote);
		assertEquals("""
						This is a multiple lines note
						This string can spans over multiple lines.""",
				multipleLinesNote.getValue());
		assertEquals("fff", multipleLinesNote.getSettings().get(NamedNoteSetting.HEADERCOLOR));
	}
	
	@Test
	void testNamedNoteExisting() {
		var dbml = """
				Table table1 {
				  table integer
				}
				
				Note single_line_note {
				  'This is a single line note'
				}
				
				Note single_line_note {
				  'This is a duplicate note'
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertEquals("[9:21] NamedNote 'single_line_note' is already defined", e.getMessage());
	}
	
	@Test
	void testOptionalRelationships() {
		var dbml = """
				Table follows {
				  following_user_id int [ref: > users.id] // optional, many-to-zero relationship
				  followed_user_id int [ref: > users.id, null] // optional, many-to-zero relationship
				}
				
				Table posts {
				  id int [pk]
				  user_id int [ref: > users.id, not null] // mandatory, many-to-one relationship
				}
				
				Table users {
				  id integer [primary key]
				  username varchar
				}""";
		var database = parse(dbml);
		
		var relationships = database.getRelationships();
		assertEquals(3, relationships.size());
		
		var schema = getDefaultSchema(database);
		var follows = schema.getTable("follows");
		assertNotNull(follows);
		var followingUserId = follows.getColumn("following_user_id");
		assertNotNull(followingUserId);
		assertNull(followingUserId.getSettings().get(ColumnSetting.NOT_NULL));
		var followedUserId = follows.getColumn("followed_user_id");
		assertNotNull(followedUserId);
		assertNull(followedUserId.getSettings().get(ColumnSetting.NOT_NULL));
		
		var posts = schema.getTable("posts");
		assertNotNull(posts);
		var userId = posts.getColumn("user_id");
		assertNotNull(userId);
		assertNotNull(userId.getSettings().get(ColumnSetting.NOT_NULL));
	}
	
	@Test
	void testTablePartialsOnly() {
		var dbml = """
				TablePartial base_template [headerColor: #ff0000] {
				  id int [pk, not null]
				  created_at timestamp [default: `now()`]
				  updated_at timestamp [default: `now()`]
				}
				
				TablePartial soft_delete_template {
				  delete_status boolean [not null]
				  deleted_at timestamp [default: `now()`]
				}
				
				TablePartial email_index {
				  email varchar [unique]
				
				  indexes {
				    email [unique]
				  }
				}""";
		var database = parse(dbml);
		
		assertFalse(database.getTablePartials().isEmpty());
		var schema = getDefaultSchema(database);
		assertNull(schema);
	}
	
	@Test
	void testTablePartials() {
		var dbml = """
				TablePartial base_template [headerColor: #ff0000] {
				  id int [pk, not null]
				  created_at timestamp [default: `now()`]
				  updated_at timestamp [default: `now()`]
				  note: "base note"
				}
				
				TablePartial soft_delete_template {
				  delete_status boolean [not null]
				  deleted_at timestamp [default: `now()`]
				}
				
				TablePartial email_index {
				  email varchar [unique]
				
				  indexes {
				    email [unique, name: 'U__email']
				  }
				}
				
				Table users {
				  ~base_template
				  ~email_index
				  name varchar
				  ~soft_delete_template
				}""";
		var database = parse(dbml);
		
		var schema = getDefaultSchema(database);
		var users = schema.getTable("users");
		assertNotNull(users);
		assertEquals("ff0000", users.getSettings().get(TableSetting.HEADERCOLOR));
		assertEquals(7, users.getColumns().size());
		var id = users.getColumn("id");
		assertNotNull(id);
		assertEquals("int", id.getType());
		assertNotNull(id.getSettings().get(ColumnSetting.PRIMARY_KEY));
		assertNotNull(id.getSettings().get(ColumnSetting.NOT_NULL));
		var deleteStatus = users.getColumn("delete_status");
		assertNotNull(deleteStatus);
		assertEquals("boolean", deleteStatus.getType());
		var name = users.getColumn("name");
		assertNotNull(name);
		assertEquals("varchar", name.getType());
		var emailIndex = users.getIndex("U__email");
		assertNotNull(emailIndex);
		var note = users.getNote();
		assertNotNull(note);
		assertEquals("base note", note.getValue());
	}
	
	@Test
	void testParseTablePartialSchema() {
		var dbml = """
				TablePartial a.base_template {
				  id int [pk, not null]
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertTrue(e.getMessage().startsWith("[1:15] unexpected token 'DOT'"));
	}
	
	@Test
	void testTablePartialsNested() {
		var dbml = """
				Table users {
				  ~soft_delete_template
				  name varchar
				}
				
				TablePartial base_template [headerColor: #ff0000] {
				  id int [pk, not null]
				  created_at timestamp [default: `now()`]
				  updated_at timestamp [default: `now()`]
				}
				
				TablePartial soft_delete_template {
				  ~base_template
				  delete_status boolean [not null]
				  deleted_at timestamp [default: `now()`]
				}""";
		var database = parse(dbml);
		
		var schema = getDefaultSchema(database);
		var users = schema.getTable("users");
		assertNotNull(users);
		assertEquals("ff0000", users.getSettings().get(TableSetting.HEADERCOLOR));
		assertEquals(6, users.getColumns().size());
		var id = users.getColumn("id");
		assertNotNull(id);
		assertEquals("int", id.getType());
		assertNotNull(id.getSettings().get(ColumnSetting.PRIMARY_KEY));
		assertNotNull(id.getSettings().get(ColumnSetting.NOT_NULL));
	}
	
	@Test
	void testTablePartialsOverride() {
		var dbml = """
				TablePartial base_template {
				  id int [pk]
				  other_column int [not null]
				
				  indexes {
				    other_column [name: '___other']
				  }
				
				  note: "base note"
				}
				
				TablePartial other_template {
				  other_column boolean [not null]
				
				  indexes {
				    other_column [unique, name: '___other']
				  }
				}
				
				Table users {
				  ~base_template
				  ~ other_template
				  id bigint [not null]
				  name varchar
				  note: "users note"
				}""";
		var database = parse(dbml);
		
		var schema = getDefaultSchema(database);
		var users = schema.getTable("users");
		assertNotNull(users);
		assertEquals(3, users.getColumns().size());
		var id = users.getColumn("id");
		assertNotNull(id);
		assertEquals("bigint", id.getType());
		assertNull(id.getSettings().get(ColumnSetting.PRIMARY_KEY));
		assertNotNull(id.getSettings().get(ColumnSetting.NOT_NULL));
		var otherColumn = users.getColumn("other_column");
		assertNotNull(otherColumn);
		assertEquals("boolean", otherColumn.getType());
		var otherIndex = users.getIndex("___other");
		assertNotNull(otherIndex);
		assertTrue(otherIndex.getSettings().containsKey(IndexSetting.UNIQUE));
		var note = users.getNote();
		assertNotNull(note);
		assertEquals("users note", note.getValue());
	}
	
	@Test
	void testTablePartialsShared() {
		var dbml = """
				TablePartial base_template {
				  id int [pk, not null]
				}
				
				Table users {
				  ~base_template
				  name varchar
				}
				
				Table posts {
				  ~base_template
				  title varchar
				}""";
		var database = parse(dbml);
		
		var schema = getDefaultSchema(database);
		var users = schema.getTable("users");
		assertNotNull(users);
		assertEquals(2, users.getColumns().size());
		var userId = users.getColumn("id");
		assertNotNull(userId);
		var posts = schema.getTable("posts");
		assertEquals(2, posts.getColumns().size());
		assertNotNull(posts);
		var postsId = posts.getColumn("id");
		assertNotNull(postsId);
	}
	
	@Test
	void testTablePartialsSharedName() {
		var dbml = """
				TablePartial tbl {
				  id int [pk, not null]
				}
				
				Table tbl {
				  ~tbl
				  name varchar
				}""";
		var database = parse(dbml);
		
		var schema = getDefaultSchema(database);
		var tbl = schema.getTable("tbl");
		assertNotNull(tbl);
		assertEquals(2, tbl.getColumns().size());
		var id = tbl.getColumn("id");
		assertNotNull(id);
		var name = tbl.getColumn("name");
		assertNotNull(name);
	}
	
	@Test
	void testTablePartialsSelfRef() {
		var dbml = """
				TablePartial tbl {
				  id int [pk, not null]
				  ~tbl
				}
				
				Table tbl {
				  ~tbl
				  name varchar
				}""";
		var database = parse(dbml);
		
		var schema = getDefaultSchema(database);
		var tbl = schema.getTable("tbl");
		assertNotNull(tbl);
		assertEquals(2, tbl.getColumns().size());
		var id = tbl.getColumn("id");
		assertNotNull(id);
		var name = tbl.getColumn("name");
		assertNotNull(name);
	}
	
	@Test
	void testTablePartialAlias() {
		var dbml = """
				TablePartial base_template as alias {
				  id int [pk, not null]
				}
				
				Table posts {
				  ~base_template
				  title varchar
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertEquals("[1:37] A TablePartial shouldn't have an alias", e.getMessage());
	}
}