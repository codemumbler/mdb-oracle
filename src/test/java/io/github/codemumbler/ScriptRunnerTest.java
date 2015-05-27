package io.github.codemumbler;

import io.github.codemumbler.cloakdb.CloakDatabase;
import org.junit.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ScriptRunnerTest {

	private static CloakDatabase database;

	@BeforeClass
	public static void setUpClass() {
		database = new CloakDatabase("jdbc/sample_db", CloakDatabase.ORACLE, "");
	}

	@After
	public void tearDown() {
		database.reset();
	}

	@Test
	public void runSimpleCreateTable() throws Exception {
		ScriptRunner runner = new ScriptRunner();
		runner.executeScript("CREATE TABLE TEST_TABLE (\n" +
				"\tTEST_COLUMN_ID NUMBER(5)\n" +
				");\n");
		Assert.assertEquals(0, runCountQuery("SELECT COUNT(*) FROM TEST_TABLE"));
	}

	private int runCountQuery(String sql) throws Exception {
		InitialContext context = new InitialContext();
		Context env = (Context) context.lookup("java:/comp/env");
		DataSource dataSource = (DataSource) env.lookup("jdbc/sample_db");
		Connection connection = dataSource.getConnection();
		Statement statement = connection.createStatement();
		ResultSet results = statement.executeQuery(sql);
		int count = 0;
		if ( results.next() )
			count = results.getInt(1);
		results.close();
		statement.close();
		connection.close();
		return count;
	}
}
