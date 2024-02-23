package com.wn.dbml.compiler;

import com.wn.dbml.compiler.lexer.LexerImpl;
import com.wn.dbml.compiler.parser.ParserImpl;
import com.wn.dbml.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParserTest {
	
	private Database parse(final String dbml) {
		return new ParserImpl().parse(new LexerImpl(dbml));
	}
	
	private Schema getDefaultSchema(final Database database) {
		return database.getSchema(Schema.DEFAULT_NAME);
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
		var database = parse(dbml);
		
		assertNotNull(database.getProject());
		var project = database.getProject();
		assertEquals("project_name", project.getName());
		var schema = database.getSchema(Schema.DEFAULT_NAME);
		assertNotNull(schema);
		assertTrue(schema.containsTable("t1"));
		assertTrue(schema.containsTable("t2"));
		assertEquals(1, database.getRelationships().size());
		assertEquals(1, schema.getTableGroups().size());
		assertEquals(1, schema.getEnums().size());
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
	void testParseComplexColumnDatatype() {
		var dbml = """
				Table users {
				  id char(8)
				  username varchar(255) [not null, unique]
				  Note: 'Stores user data'
				}""";
		var database = parse(dbml);
		
		var schema = getDefaultSchema(database);
		var table = schema.getTable("users");
		assertEquals("char(8)", table.getColumn("id").getType());
		assertEquals("varchar(255)", table.getColumn("username").getType());
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
				
				TableGroup a.tablegroup_name {
				  table1
				  table2
				  C
				}""";
		var database = parse(dbml);
		
		var schema = database.getSchema("a");
		assertNotNull(schema);
		var tableGroupName = "tablegroup_name";
		var tableGroup = schema.getTableGroup(tableGroupName);
		assertNotNull(tableGroup);
		assertTrue(schema.containsTableGroup(tableGroupName));
		var tables = tableGroup.getTables();
		assertEquals(3, tables.size());
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
				
				Ref r1: table2.column2 <> table1.column1""";
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
				
				Ref r1: table2.(id, column2) <> table1.(id, column1)""";
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
				  table2.column2 < table1.column1 [update: set null, delete: cascade]
				}""";
		var database = parse(dbml);
		
		var relationships = database.getRelationships();
		assertEquals(1, relationships.size());
		var ref = relationships.iterator().next();
		assertEquals("<", ref.getRelation().getSymbol());
		validateRefColumn(ref.getFrom(), 1, "table2.column2");
		validateRefColumn(ref.getTo(), 1, "table1.column1");
		var settings = ref.getSettings();
		assertEquals(2, settings.size());
		var update = settings.get(RelationshipSetting.UPDATE);
		assertEquals("set null", update);
		var delete = settings.get(RelationshipSetting.DELETE);
		assertEquals("cascade", delete);
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
		assertEquals("integer", table.getColumn("table").getType());
	}
	
	@Test
	void testKeywordLiteralSubstitutionMulti() {
		var dbml = """
				Table table1 {
				  not null integer
				}""";
		
		var e = assertThrows(ParsingException.class, () -> parse(dbml));
		assertTrue(e.getMessage().contains("not null"));
		assertEquals(2, e.getPosition().line());
		assertEquals(10, e.getPosition().column());
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
}