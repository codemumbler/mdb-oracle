package io.github.codemumbler;

import io.github.codemumbler.datatype.DoubleDataType;
import io.github.codemumbler.datatype.IntegerDataType;
import io.github.codemumbler.datatype.Memo;
import io.github.codemumbler.datatype.Text;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class OracleScriptWriterTest {

	private Database database;
	private OracleScriptWriter writer;

	@Before
	public void setUp() {
		database = new Database();
		writer = new OracleScriptWriter(database);
		database.setSchemaName("TEST_SCHEMA");
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
		Table table = new Table();
		table.setName("testTable");
		writer.writeOneTable(table);
	}

	@Test
	public void oneTableColumns() {
		Table table = new Table();
		table.setName("testTable");
		Column column = new Column();
		column.setName("testColumnID");
		column.setDataType(new IntegerDataType());
		column.setLength(5);
		table.addColumn(column);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_COLUMN_ID NUMBER(5)\n" +
				");\n", writer.writeOneTable(table));
	}

	@Test
	public void memoDataType() {
		Table table = new Table();
		table.setName("testTable");
		Column column = new Column();
		column.setName("testMemo");
		column.setDataType(new Memo());
		table.addColumn(column);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_MEMO CLOB\n" +
				");\n", writer.writeOneTable(table));
	}

	@Test
	public void doubleDataType() {
		Table table = new Table();
		table.setName("testTable");
		Column column = new Column();
		column.setName("testDouble");
		column.setDataType(new DoubleDataType());
		column.setLength(5);
		column.setPrecision(2);
		table.addColumn(column);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_DOUBLE NUMBER(5,2)\n" +
				");\n", writer.writeOneTable(table));
	}

	@Test
	public void requiredColumn() {
		Table table = new Table();
		table.setName("testTable");
		Column column = new Column();
		column.setName("testID");
		column.setDataType(new IntegerDataType());
		column.setLength(5);
		column.setRequired(true);
		table.addColumn(column);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_ID NUMBER(5) NOT NULL\n" +
				");\n", writer.writeOneTable(table));
	}

	@Test
	public void oneTableTextColumn() {
		Table table = new Table();
		table.setName("testTable");
		Column column = new Column();
		column.setName("testLabel");
		column.setDataType(new Text());
		column.setLength(5);
		table.addColumn(column);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_LABEL VARCHAR2(5)\n" +
				");\n", writer.writeOneTable(table));
	}

	@Test
	public void oneTableTwoColumns() {
		Table table = new Table();
		table.setName("testTable");
		Column column = new Column();
		column.setName("testColumnID");
		column.setDataType(new IntegerDataType());
		column.setLength(5);
		table.addColumn(column);
		column = new Column();
		column.setName("testLabel");
		column.setDataType(new Text());
		column.setLength(100);
		table.addColumn(column);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_COLUMN_ID NUMBER(5),\n" +
				"\tTEST_LABEL VARCHAR2(100)\n" +
				");\n", writer.writeOneTable(table));
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
