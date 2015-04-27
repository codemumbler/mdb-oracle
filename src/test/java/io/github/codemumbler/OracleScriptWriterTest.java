package io.github.codemumbler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

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

	@Test
	public void oneTableColumns() {
		Table table = new Table();
		table.setName("TEST_TABLE");
		Column column = new Column();
		column.setName("TEST_COLUMN_ID");
		column.setDataType(DataType.INTEGER);
		column.setLength(5);
		table.addColumn(column);
		Assert.assertEquals("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_COLUMN_ID NUMBER(5)\n" +
				");", writer.writeOneTable(table));
	}
}
