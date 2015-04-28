package io.github.codemumbler;

import io.github.codemumbler.datatype.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class OracleScriptWriterTest {

	private Database database;
	private OracleScriptWriter writer;
	private Table table;

	@Before
	public void setUp() {
		database = new Database();
		writer = new OracleScriptWriter(database);
		database.setSchemaName("TEST_SCHEMA");
		table = new Table();
		table.setName("testTable");
		database.addTable(table);
	}

	@Test
	public void convertEmptySchema() {
		database.setSchemaName("EmptySchema");
		Assert.assertEquals("--CREATE USER EmptySchema IDENTIFIED BY password2Change;\n" +
						"-- GRANT CONNECT, RESOURCE, CREATE SESSION, CREATE TABLE, CREATE VIEW,\n" +
						"-- \tCREATE PROCEDURE,CREATE SYNONYM, CREATE SEQUENCE, CREATE TRIGGER TO EmptySchema;\n",
				writer.writeSchemaScript());
	}

	@Test(expected = OracleScriptWriterException.class)
	public void tableWithNoColumns() {
		writer.writeTableScript(table);
	}

	@Test
	public void oneTableColumns() {
		addColumnToTable("testColumnID", new IntegerDataType(), 5);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_COLUMN_ID NUMBER(5)\n" +
				");\n", writer.writeTableScript(table));
	}

	@Test
	public void memoDataType() {
		addColumnToTable("testMemo", new Memo(), -1);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_MEMO CLOB\n" +
				");\n", writer.writeTableScript(table));
	}

	@Test
	public void doubleDataType() {
		Column column = addColumnToTable("testDouble", new DoubleDataType(), 5);
		column.setPrecision(2);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_DOUBLE NUMBER(5,2)\n" +
				");\n", writer.writeTableScript(table));
	}

	@Test
	public void booleanDataType() {
		addColumnToTable("testBoolean", new BooleanDataType(), 1);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_BOOLEAN VARCHAR2(1)\n" +
				");\n", writer.writeTableScript(table));
	}

	@Test
	public void DateTimeType() {
		addColumnToTable("testDateTime", new DateTime(), -1);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_DATE_TIME TIMESTAMP\n" +
				");\n", writer.writeTableScript(table));
	}

	@Test(expected = OracleScriptWriterException.class)
	public void unknownDataTypeThrowsException() {
		addColumnToTable("testNull", new NullDataType(), -1);
		writer.writeTableScript(table);
	}

	@Test
	public void requiredColumn() {
		Column column = addColumnToTable("testID", new IntegerDataType(), 5);
		column.setRequired(true);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_ID NUMBER(5) NOT NULL\n" +
				");\n", writer.writeTableScript(table));
	}

	@Test
	public void oneTableTextColumn() {
		addColumnToTable("testLabel", new Text(), 5);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_LABEL VARCHAR2(5)\n" +
				");\n", writer.writeTableScript(table));
	}

	@Test
	public void currencyColumn() {
		Column column = addColumnToTable("testMoney", new CurrencyDataType(), 5);
		column.setPrecision(2);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_MONEY NUMBER(5,2)\n" +
				");\n", writer.writeTableScript(table));
	}

	@Test
	public void oneTableTwoColumns() {
		addColumnToTable("testColumnID", new IntegerDataType(), 5);
		addColumnToTable("testLabel", new Text(), 100);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_COLUMN_ID NUMBER(5),\n" +
				"\tTEST_LABEL VARCHAR2(100)\n" +
				");\n", writer.writeTableScript(table));
	}

	@Test
	public void primaryColumn() {
		Column column = addColumnToTable("testColumnID", new IntegerDataType(), 5);
		column.setPrimary(true);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
						"\tTEST_COLUMN_ID NUMBER(5)\n" +
						");\n" +
						"\nCREATE UNIQUE INDEX TEST_TABLE_UK1 ON TEST_TABLE (TEST_COLUMN_ID);\n" +
						"\nALTER TABLE TEST_TABLE ADD CONSTRAINT TEST_TABLE_PK PRIMARY KEY (TEST_COLUMN_ID) ENABLE;\n",
				writer.writeTableScript(table));
	}

	@Test
	public void sequenceForAutoIncrement() {
		Column column = addColumnToTable("testColumnID", new IntegerDataType(), 5);
		column.setAutoIncrement(true);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
						"\tTEST_COLUMN_ID NUMBER(5)\n" +
						");\n" +
						"\nCREATE SEQUENCE TEST_TABLE_SEQ MINVALUE 1 MAXVALUE 99999 INCREMENT BY 1 START WITH 0 NOCACHE NOORDER NOCYCLE;\n" +
						"\nCREATE OR REPLACE TRIGGER TEST_TABLE_TRIG\n" +
						"BEFORE INSERT ON TEST_TABLE\n" +
						"FOR EACH ROW BEGIN\n" +
						"\tIF :NEW.TEST_COLUMN_ID IS NULL THEN\n" +
						"\t\tSELECT TEST_TABLE_SEQ.nextVal\n" +
						"\t\tINTO :NEW.TEST_COLUMN_ID\n" +
						"\t\tFROM dual;\n" +
						"\tEND IF;\n" +
						"END;\n" +
						"/\n" +
						"ALTER TRIGGER TEST_TABLE_TRIG ENABLE;",
				writer.writeTableScript(table));
	}

	@Test
	public void sequenceStartingValue() {
		Column column = addColumnToTable("testColumnID", new IntegerDataType(), 5);
		column.setAutoIncrement(true);
		table.setNextValue(2);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
						"\tTEST_COLUMN_ID NUMBER(5)\n" +
						");\n" +
						"\nCREATE SEQUENCE TEST_TABLE_SEQ MINVALUE 1 MAXVALUE 99999 INCREMENT BY 1 START WITH 2 NOCACHE NOORDER NOCYCLE;\n" +
						"\nCREATE OR REPLACE TRIGGER TEST_TABLE_TRIG\n" +
						"BEFORE INSERT ON TEST_TABLE\n" +
						"FOR EACH ROW BEGIN\n" +
						"\tIF :NEW.TEST_COLUMN_ID IS NULL THEN\n" +
						"\t\tSELECT TEST_TABLE_SEQ.nextVal\n" +
						"\t\tINTO :NEW.TEST_COLUMN_ID\n" +
						"\t\tFROM dual;\n" +
						"\tEND IF;\n" +
						"END;\n" +
						"/\n" +
						"ALTER TRIGGER TEST_TABLE_TRIG ENABLE;",
				writer.writeTableScript(table));
	}

	@Test
	public void sequenceMaximumValue() {
		Column column = addColumnToTable("testColumnID", new IntegerDataType(), 9);
		column.setAutoIncrement(true);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
						"\tTEST_COLUMN_ID NUMBER(9)\n" +
						");\n" +
						"\nCREATE SEQUENCE TEST_TABLE_SEQ MINVALUE 1 MAXVALUE 999999999 INCREMENT BY 1 START WITH 0 NOCACHE NOORDER NOCYCLE;\n" +
						"\nCREATE OR REPLACE TRIGGER TEST_TABLE_TRIG\n" +
						"BEFORE INSERT ON TEST_TABLE\n" +
						"FOR EACH ROW BEGIN\n" +
						"\tIF :NEW.TEST_COLUMN_ID IS NULL THEN\n" +
						"\t\tSELECT TEST_TABLE_SEQ.nextVal\n" +
						"\t\tINTO :NEW.TEST_COLUMN_ID\n" +
						"\t\tFROM dual;\n" +
						"\tEND IF;\n" +
						"END;\n" +
						"/\n" +
						"ALTER TRIGGER TEST_TABLE_TRIG ENABLE;",
				writer.writeTableScript(table));
	}

	@Test
	public void cleanName_alreadyUpperCase() {
		Assert.assertEquals("TABLE1", writer.cleanName("TABLE1"));
	}

	@Test
	public void cleanName_toUpperCase() {
		Assert.assertEquals("PROJECTS", writer.cleanName("projects"));
	}

	@Test
	public void cleanName_separateCamelCase() {
		Assert.assertEquals("TEST_SCHEMA", writer.cleanName("testSchema"));
	}

	@Test
	public void cleanName_retainAcronymsBeforeWord() {
		Assert.assertEquals("EMP_LEVEL", writer.cleanName("EMPLevel"));
	}

	@Test
	public void cleanName_retainAcronymsAfterWord() {
		Assert.assertEquals("ACCESS_LEVEL_ID", writer.cleanName("AccessLevelID"));
	}

	@Test
	public void cleanName_DoNotAddExtraUnderscores() {
		Assert.assertEquals("ACCESS_LEVEL_ID", writer.cleanName("Access_LevelID"));
	}

	@Test
	public void cleanName_removeSpacesAndBrackets() {
		Assert.assertEquals("NAME_FULL", writer.cleanName("Name (Full)"));
	}

	@Test
	public void cleanName_secondWordAcronym() {
		Assert.assertEquals("NEW_EMP", writer.cleanName("New EMP"));
	}

	@Test
	public void cleanName_AddUnderscoreToReservedWords() {
		Assert.assertEquals("SQL_", writer.cleanName("SQL"));
	}

	@Test
	public void cleanName_upperCaseReservedWords() {
		Assert.assertEquals("GROUP_", writer.cleanName("group"));
	}

	@Test
	public void writeSimpleData() {
		Column id = addColumnToTable("ID", new IntegerDataType(), 5);
		Column label = addColumnToTable("label", new Text(), 15);
		Row data = new Row(table);
		data.add(id, 1);
		data.add(label, "label1");
		table.addRow(data);
		Assert.assertEquals("INSERT INTO TEST_TABLE(ID, LABEL) VALUES (1, 'label1');\n",
				writer.writeTableInsertions(table));
	}

	@Test
	public void writeNullFieldData() {
		Column id = addColumnToTable("ID", new IntegerDataType(), 5);
		addColumnToTable("label", new Text(), 15);
		Row data = new Row(table);
		data.add(id, 1);
		table.addRow(data);
		Assert.assertEquals("INSERT INTO TEST_TABLE(ID, LABEL) VALUES (1, NULL);\n",
				writer.writeTableInsertions(table));
	}

	@Test
	public void writeClobFieldData() {
		Column id = addColumnToTable("ID", new IntegerDataType(), 5);
		id.setPrimary(true);
		Column clob = addColumnToTable("MEMO", new Memo(), -1);
		Row data = new Row(table);
		data.add(id, 1);
		data.add(clob, "Some really long text");
		table.addRow(data);
		Assert.assertEquals("INSERT INTO TEST_TABLE(ID) VALUES (1);\n" +
						"DECLARE\nstr varchar2(32767);\nBEGIN\n\tstr := 'Some really long text';\n" +
						"UPDATE TEST_TABLE SET MEMO = str WHERE ID = 1;\nEND;\n/\n",
				writer.writeTableInsertions(table));
	}

	@Test
	public void nullClobFieldData() {
		Column id = addColumnToTable("ID", new IntegerDataType(), 5);
		id.setPrimary(true);
		Column clob = addColumnToTable("MEMO", new Memo(), -1);
		Row data = new Row(table);
		data.add(id, 1);
		data.add(clob, null);
		table.addRow(data);
		Assert.assertEquals("INSERT INTO TEST_TABLE(ID) VALUES (1);\n",
				writer.writeTableInsertions(table));
	}

	@Test
	public void noPrimaryKeyClobFieldDataUsesAllColumns() {
		Column id = addColumnToTable("ID", new IntegerDataType(), 5);
		Column label = addColumnToTable("LABEL", new Text(), 100);
		Column clob = addColumnToTable("MEMO", new Memo(), -1);
		Row data = new Row(table);
		data.add(id, 1);
		data.add(label, "label1");
		data.add(clob, "Memo for row 1");
		table.addRow(data);
		Assert.assertEquals("INSERT INTO TEST_TABLE(ID, LABEL) VALUES (1, 'label1');\n" +
						"DECLARE\nstr varchar2(32767);\nBEGIN\n\tstr := 'Memo for row 1';\n" +
						"UPDATE TEST_TABLE SET MEMO = str WHERE ID = 1 AND LABEL = 'label1';\nEND;\n/\n",
				writer.writeTableInsertions(table));
	}

	@Test
	public void multipleInsertions() {
		Column id = addColumnToTable("ID", new IntegerDataType(), 5);
		Column label = addColumnToTable("label", new Text(), 15);
		Row data = new Row(table);
		data.add(id, 1);
		data.add(label, "label1");
		table.addRow(data);
		data = new Row(table);
		data.add(id, 2);
		data.add(label, "label2");
		table.addRow(data);
		Assert.assertEquals("INSERT INTO TEST_TABLE(ID, LABEL) VALUES (1, 'label1');\n" +
						"INSERT INTO TEST_TABLE(ID, LABEL) VALUES (2, 'label2');\n",
				writer.writeTableInsertions(table));
	}

	@Test(expected = OracleScriptWriterException.class)
	public void tableWithUnknownDataType() {
		Column nullable = addColumnToTable("Nullable", new NullDataType(), -1);
		Row data = new Row(table);
		data.add(nullable, 1);
		table.addRow(data);
		writer.writeTableInsertions(table);
	}

	@Test
	public void writeForeignKeys() {
		Table parentTable = table;
		Column id = addColumnToTable("ID", new IntegerDataType(), 5);
		id.setPrimary(true);
		Table childTable = new Table();
		childTable.setName("CHILD_TABLE");
		database.addTable(childTable);
		table = childTable;
		Column childColumn = addColumnToTable("FOREIGN_ID", new IntegerDataType(), 5);
		childColumn.setForeignKey(true);
		ForeignKey foreignKey = new ForeignKey();
		foreignKey.setChildColumn(childColumn);
		foreignKey.setParentTable(parentTable);
		foreignKey.setParentColumn(id);
		childTable.addForeignKey(foreignKey);
		Assert.assertEquals("\nALTER TABLE CHILD_TABLE ADD CONSTRAINT CHILD_TABLE_FK1 FOREIGN KEY (FOREIGN_ID)\n" +
				"\tREFERENCES TEST_TABLE (ID) ENABLE;\n", writer.writeForeignKey(childTable));
	}

	@Test
	public void notRequiredForeignKeyConvertFromAccessZeroToOracleNull() {
		Table parentTable = table;
		Column id = addColumnToTable("ID", new IntegerDataType(), 5);
		id.setPrimary(true);
		Row data = new Row(table);
		data.add(id, 1);
		table.addRow(data);
		Table childTable = new Table();
		childTable.setName("CHILD_TABLE");
		database.addTable(childTable);
		table = childTable;
		Column childColumn = addColumnToTable("FOREIGN_ID", new IntegerDataType(), 5);
		data = new Row(table);
		data.add(childColumn, 0);
		table.addRow(data);
		childColumn.setForeignKey(true);
		ForeignKey foreignKey = new ForeignKey();
		foreignKey.setChildColumn(childColumn);
		foreignKey.setParentTable(parentTable);
		foreignKey.setParentColumn(id);
		childTable.addForeignKey(foreignKey);
		Assert.assertEquals("INSERT INTO CHILD_TABLE(FOREIGN_ID) VALUES (NULL);\n", writer.writeTableInsertions(childTable));
	}

	@Test
	public void notFoundForeignKeyValueWritesCommentedOutInsert() {
		Table parentTable = table;
		Column id = addColumnToTable("ID", new IntegerDataType(), 5);
		id.setPrimary(true);
		Row data = new Row(table);
		data.add(id, 1);
		table.addRow(data);
		Table childTable = new Table();
		childTable.setName("CHILD_TABLE");
		database.addTable(childTable);
		table = childTable;
		Column childColumn = addColumnToTable("FOREIGN_ID", new IntegerDataType(), 5);
		data = new Row(table);
		data.add(childColumn, 2);
		table.addRow(data);
		childColumn.setRequired(true);
		childColumn.setForeignKey(true);
		ForeignKey foreignKey = new ForeignKey();
		foreignKey.setChildColumn(childColumn);
		foreignKey.setParentTable(parentTable);
		foreignKey.setParentColumn(id);
		childTable.addForeignKey(foreignKey);
		Assert.assertEquals("--INSERT INTO CHILD_TABLE(FOREIGN_ID) VALUES (2);\n", writer.writeTableInsertions(childTable));
	}

	@Test
	public void textNotFoundForeignKeyValueWritesCommentedOutInsert() {
		Table parentTable = table;
		Column id = addColumnToTable("PROV", new Text(), 5);
		id.setPrimary(true);
		Row data = new Row(table);
		data.add(id, "ON");
		table.addRow(data);
		Table childTable = new Table();
		childTable.setName("CHILD_TABLE");
		database.addTable(childTable);
		table = childTable;
		Column childColumn = addColumnToTable("FOREIGN_ID", new Text(), 5);
		data = new Row(table);
		data.add(childColumn, "AB");
		table.addRow(data);
		childColumn.setRequired(true);
		childColumn.setForeignKey(true);
		ForeignKey foreignKey = new ForeignKey();
		foreignKey.setChildColumn(childColumn);
		foreignKey.setParentTable(parentTable);
		foreignKey.setParentColumn(id);
		childTable.addForeignKey(foreignKey);
		Assert.assertEquals("--INSERT INTO CHILD_TABLE(FOREIGN_ID) VALUES ('AB');\n", writer.writeTableInsertions(childTable));
	}

	@Test
	public void writeFullDDLScript() {
		createDatabase();
		Assert.assertEquals("--CREATE USER TEST_SCHEMA IDENTIFIED BY password2Change;\n" +
				"-- GRANT CONNECT, RESOURCE, CREATE SESSION, CREATE TABLE, CREATE VIEW,\n" +
				"-- \tCREATE PROCEDURE,CREATE SYNONYM, CREATE SEQUENCE, CREATE TRIGGER TO TEST_SCHEMA;\n" +
				"CREATE TABLE TEST_TABLE (\n" +
				"\tID NUMBER(5)\n" +
				");\n" +
				"\n" +
				"CREATE UNIQUE INDEX TEST_TABLE_UK1 ON TEST_TABLE (ID);\n" +
				"\n" +
				"ALTER TABLE TEST_TABLE ADD CONSTRAINT TEST_TABLE_PK PRIMARY KEY (ID) ENABLE;\n" +
				"CREATE TABLE CHILD_TABLE (\n" +
				"\tFOREIGN_ID NUMBER(5) NOT NULL\n" +
				");\n" +
				"\n" +
				"ALTER TABLE CHILD_TABLE ADD CONSTRAINT CHILD_TABLE_FK1 FOREIGN KEY (FOREIGN_ID)\n" +
				"\tREFERENCES TEST_TABLE (ID) ENABLE;\n", writer.writeDDLScript());
	}

	@Test
	public void writeFullInsertionScript() {
		createDatabase();
		Assert.assertEquals("INSERT INTO TEST_TABLE(ID) VALUES (1);\n" +
				"INSERT INTO CHILD_TABLE(FOREIGN_ID) VALUES (1);\n", writer.writeDatabaseInsertions());
	}

	@Test
	public void writeFullScript() {
		createDatabase();
		Assert.assertEquals("--CREATE USER TEST_SCHEMA IDENTIFIED BY password2Change;\n" +
				"-- GRANT CONNECT, RESOURCE, CREATE SESSION, CREATE TABLE, CREATE VIEW,\n" +
				"-- \tCREATE PROCEDURE,CREATE SYNONYM, CREATE SEQUENCE, CREATE TRIGGER TO TEST_SCHEMA;\n" +
				"CREATE TABLE TEST_TABLE (\n" +
				"\tID NUMBER(5)\n" +
				");\n" +
				"\n" +
				"CREATE UNIQUE INDEX TEST_TABLE_UK1 ON TEST_TABLE (ID);\n" +
				"\n" +
				"ALTER TABLE TEST_TABLE ADD CONSTRAINT TEST_TABLE_PK PRIMARY KEY (ID) ENABLE;\n" +
				"CREATE TABLE CHILD_TABLE (\n" +
				"\tFOREIGN_ID NUMBER(5) NOT NULL\n" +
				");\n" +
				"INSERT INTO TEST_TABLE(ID) VALUES (1);\n" +
				"INSERT INTO CHILD_TABLE(FOREIGN_ID) VALUES (1);\n" +
				"\n" +
				"ALTER TABLE CHILD_TABLE ADD CONSTRAINT CHILD_TABLE_FK1 FOREIGN KEY (FOREIGN_ID)\n" +
				"\tREFERENCES TEST_TABLE (ID) ENABLE;\n", writer.writeScript());
	}

	private void createDatabase() {
		Table parentTable = table;
		Column id = addColumnToTable("ID", new IntegerDataType(), 5);
		id.setPrimary(true);
		Row data = new Row(table);
		data.add(id, 1);
		table.addRow(data);
		Table childTable = new Table();
		childTable.setName("CHILD_TABLE");
		database.addTable(childTable);
		table = childTable;
		Column childColumn = addColumnToTable("FOREIGN_ID", new IntegerDataType(), 5);
		data = new Row(table);
		data.add(childColumn, 1);
		table.addRow(data);
		childColumn.setRequired(true);
		childColumn.setForeignKey(true);
		ForeignKey foreignKey = new ForeignKey();
		foreignKey.setChildColumn(childColumn);
		foreignKey.setParentTable(parentTable);
		foreignKey.setParentColumn(id);
		childTable.addForeignKey(foreignKey);
	}


	private Column addColumnToTable(String name, DataType type, int length) {
		Column column = new Column();
		column.setName(name);
		column.setDataType(type);
		column.setLength(length);
		table.addColumn(column);
		return column;
	}
}
