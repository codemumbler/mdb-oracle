package io.github.codemumbler;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ScriptRunner {

	private DataSource getDataSource() throws Exception  {
		InitialContext context = new InitialContext();
		Context env = (Context) context.lookup("java:/comp/env");
		return (DataSource) env.lookup("jdbc/sample_db");
	}

	public void executeScript(String s) throws Exception {
		try (Connection connection = getDataSource().getConnection();
			Statement statement = connection.createStatement()) {
			statement.executeUpdate(s);
		}
	}
}
