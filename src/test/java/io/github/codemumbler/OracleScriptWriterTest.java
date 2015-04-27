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
	}

	@Test
	public void convertEmptySchema() {
		database.setSchemaName("EmptySchema");
		Assert.assertEquals("--CREATE USER EmptySchema IDENTIFIED BY password2Change;\n" +
						"-- GRANT CONNECT, RESOURCE, CREATE SESSION, CREATE TABLE, CREATE VIEW,\n" +
						"-- \tCREATE PROCEDURE,CREATE SYNONYM, CREATE SEQUENCE, CREATE TRIGGER TO EmptySchema;\n",
				writer.writeScript());
	}

	@Test(expected = OracleScriptWriterException.class)
	public void tableWithNoColumns() {
		writer.writeOneTable(table);
	}

	@Test
	public void oneTableColumns() {
		addColumnToTable("testColumnID", new IntegerDataType(), 5);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_COLUMN_ID NUMBER(5)\n" +
				");\n", writer.writeOneTable(table));
	}

	@Test
	public void memoDataType() {
		addColumnToTable("testMemo", new Memo(), 0);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_MEMO CLOB\n" +
				");\n", writer.writeOneTable(table));
	}

	@Test
	public void doubleDataType() {
		addColumnToTable("testDouble", new DoubleDataType(), 5, 2, false);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_DOUBLE NUMBER(5,2)\n" +
				");\n", writer.writeOneTable(table));
	}

	@Test
	public void booleanDataType() {
		addColumnToTable("testBoolean", new BooleanDataType(), 1);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_BOOLEAN VARCHAR2(1)\n" +
				");\n", writer.writeOneTable(table));
	}

	@Test
	public void requiredColumn() {
		addColumnToTable("testID", new IntegerDataType(), 5, -1, true);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_ID NUMBER(5) NOT NULL\n" +
				");\n", writer.writeOneTable(table));
	}

	@Test
	public void oneTableTextColumn() {
		addColumnToTable("testLabel", new Text(), 5);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_LABEL VARCHAR2(5)\n" +
				");\n", writer.writeOneTable(table));
	}

	@Test
	public void oneTableTwoColumns() {
		addColumnToTable("testColumnID", new IntegerDataType(), 5);
		addColumnToTable("testLabel", new Text(), 100);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_COLUMN_ID NUMBER(5),\n" +
				"\tTEST_LABEL VARCHAR2(100)\n" +
				");\n", writer.writeOneTable(table));
	}

	private void addColumnToTable(String name, DataType type, int length, int precision, boolean required) {
		Column column = new Column();
		column.setName(name);
		column.setDataType(type);
		column.setLength(length);
		column.setPrecision(precision);
		column.setRequired(required);
		table.addColumn(column);
	}

	private void addColumnToTable(String name, DataType type, int length) {
		addColumnToTable(name, type, length, -1, false);
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
}
