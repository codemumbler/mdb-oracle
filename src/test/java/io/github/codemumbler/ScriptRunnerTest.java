package io.github.codemumbler;

import io.github.codemumbler.cloakdb.CloakAbstractTestCase;
import io.github.codemumbler.cloakdb.CloakDatabase;
import io.github.codemumbler.datatype.DataType;
import io.github.codemumbler.datatype.IntegerDataType;
import org.junit.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ScriptRunnerTest extends CloakAbstractTestCase {

	private ScriptRunner runner;
	private Database database;

	@Override
	protected String jdbcName() {
		return "jdbc/sample_db";
	}

	@Override
	protected int dialect() {
		return CloakDatabase.ORACLE;
	}

	@Before
	public void setUp() {
		runner = new ScriptRunner();
		database = new Database();
	}

	@Test(expected = SQLException.class)
	public void emptyString() throws Exception {
		runner.executeScript("");
		runCountQuery();
	}

	@Test(expected = SQLException.class)
	public void nullString() throws Exception {
		runner.executeScript(null);
		runCountQuery();
	}

	@Test
	public void runSimpleCreateTable() throws Exception {
		runner.executeScript("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_COLUMN_ID NUMBER(5)\n" +
				");\n");
		Assert.assertEquals(0, runCountQuery());
	}

	@Test
	public void multipleStatements() throws Exception {
		runner.executeScript("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_COLUMN_ID NUMBER(5)\n" +
				");\nINSERT INTO TEST_TABLE (TEST_COLUMN_ID) VALUES (1);");
		Assert.assertEquals(1, runCountQuery());
	}

	@Test
	public void ignoreComments() throws Exception {
		runner.executeScript("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_COLUMN_ID NUMBER(5)\n" +
				");\n--INSERT INTO TEST_TABLE (TEST_COLUMN_ID) VALUES (1);");
		Assert.assertEquals(0, runCountQuery());
	}

	@Test
	public void buildFromDatabaseObject() throws Exception {
		Table table = new Table();
		table.setName("TEST_TABLE");
		database.addTable(table);
		addColumnToTable(table, "TEST_COLUMN_ID", new IntegerDataType(), 5);
		runner.executeCreation(database);
		Assert.assertEquals(0, runCountQuery());
	}

	private int runCountQuery() throws Exception {
		InitialContext context = new InitialContext();
		Context env = (Context) context.lookup("java:/comp/env");
		DataSource dataSource = (DataSource) env.lookup("jdbc/sample_db");
		Connection connection = dataSource.getConnection();
		Statement statement = connection.createStatement();
		ResultSet results = statement.executeQuery("SELECT COUNT(*) FROM TEST_TABLE");
		int count = 0;
		if ( results.next() )
			count = results.getInt(1);
		results.close();
		statement.close();
		connection.close();
		return count;
	}

	private Column addColumnToTable(Table table, String name, DataType type, int length) {
		Column column = new Column();
		column.setName(name);
		column.setDataType(type);
		column.setLength(length);
		table.addColumn(column);
		return column;
	}
}
