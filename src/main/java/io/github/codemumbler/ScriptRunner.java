package io.github.codemumbler;

import oracle.jdbc.pool.OracleDataSource;

import javax.sql.DataSource;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ScriptRunner {

	private final DataSource dataSource;

	public ScriptRunner(final String jdbcUrl, final String username, final String password) throws SQLException {
		OracleDataSource ds = new OracleDataSource();
		ds.setURL(jdbcUrl);
		ds.setUser(username);
		ds.setPassword(password);
		this.dataSource = ds;
	}

	public ScriptRunner(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public void executeScript(final String sql) throws Exception {
		if ( sql == null )
			return;
		StringBuilder sqlStatement = null;
		try (Connection connection = dataSource.getConnection()) {
			connection.setAutoCommit(true);
			LineNumberReader lineReader = new LineNumberReader(new StringReader(sql));
			String line;
			int closeableStatements = 0;
			while ((line = lineReader.readLine()) != null) {
				line = line.trim();
				if (sqlStatement == null)
					sqlStatement = new StringBuilder();
				if (line.isEmpty() || line.startsWith("--") || line.startsWith("/"))
					continue;
				else {
					sqlStatement.append(line).append("\n");
					if ((line.contains("FOR ") && !line.contains("END;")) ||
							(line.contains("IF") && !line.contains("END IF;")) ||
							(line.contains("DECLARE") && !line.contains("END;")))
						closeableStatements++;
					if (line.contains("END IF;") || line.contains("END;"))
						closeableStatements--;
				}
				if (line.contains(";") && closeableStatements <= 0) {
					//remove last semi-colon?
					if (!sqlStatement.toString().toUpperCase().endsWith("END;\n") )
						sqlStatement = sqlStatement.delete(sqlStatement.lastIndexOf(";"), sqlStatement.length()-1);
					try (Statement statement = connection.createStatement()){
						statement.execute(sqlStatement.toString());
					} catch (Exception e) {
						throw new MDBException("The follow SQL statement failed: " + sqlStatement.toString(), e);
					}
					sqlStatement = null;
					closeableStatements = 0;
				}
			}
		}
	}

	public void executeCreation(final Database database) throws Exception {
		OracleScriptWriter writer = new OracleScriptWriter(database);
		executeScript(writer.writeScript());
	}
}
