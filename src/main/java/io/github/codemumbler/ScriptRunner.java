package io.github.codemumbler;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

public class ScriptRunner {

	private DataSource getDataSource() throws Exception  {
		InitialContext context = new InitialContext();
		Context env = (Context) context.lookup("java:/comp/env");
		return (DataSource) env.lookup("jdbc/sample_db");
	}

	public void executeScript(String sql) throws Exception {
		if ( sql == null )
			return;
		String[] sqlStatements = sql.trim().split(";");
		for ( String sqlStatement : sqlStatements ) {
			sqlStatement = sqlStatement.trim();
			if (sqlStatement.startsWith("--"))
				continue;
			try (Connection connection = getDataSource().getConnection();
				 Statement statement = connection.createStatement()) {
				statement.executeUpdate(sqlStatement);
			}
		}
	}

	public void executeCreation(Database database) throws Exception {
		OracleScriptWriter writer = new OracleScriptWriter(database);
		executeScript(writer.writeScript());
	}
}
