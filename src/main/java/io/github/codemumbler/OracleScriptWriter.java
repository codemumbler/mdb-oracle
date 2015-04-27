package io.github.codemumbler;

public class OracleScriptWriter {

	private static final String SCHEMA_CREATION = "--CREATE USER %1$s IDENTIFIED BY password2Change;\n" +
			"-- GRANT CONNECT, RESOURCE, CREATE SESSION, CREATE TABLE, CREATE VIEW,\n" +
			"-- \tCREATE PROCEDURE,CREATE SYNONYM, CREATE SEQUENCE, CREATE TRIGGER TO %1$s;\n";
	private final Database database;

	public OracleScriptWriter(Database database) {
		this.database = database;
	}

	public String writeScript() {
		StringBuilder script = new StringBuilder();
		script.append(String.format(SCHEMA_CREATION, database.getSchemaName()));
		return script.toString();
	}
}
