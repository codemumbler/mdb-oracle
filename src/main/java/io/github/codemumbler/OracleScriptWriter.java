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

	public String writeOneTable(Table table) {
		StringBuilder tableCreateScript = new StringBuilder("CREATE TABLE ");
		tableCreateScript.append(table.getName()).append(" (\n");
		for ( Column column : table.getColumns() ) {
			tableCreateScript.append("\t").append(column.getName()).append(" NUMBER(").append(column.getLength()).append(")\n");
		}
		tableCreateScript.append(");");
		return tableCreateScript.toString();
	}
}
