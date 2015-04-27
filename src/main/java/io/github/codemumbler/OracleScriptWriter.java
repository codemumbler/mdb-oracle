package io.github.codemumbler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OracleScriptWriter {

	private static final String SCHEMA_CREATION = "--CREATE USER %1$s IDENTIFIED BY password2Change;\n" +
			"-- GRANT CONNECT, RESOURCE, CREATE SESSION, CREATE TABLE, CREATE VIEW,\n" +
			"-- \tCREATE PROCEDURE,CREATE SYNONYM, CREATE SEQUENCE, CREATE TRIGGER TO %1$s;\n";
	private static final List<String> RESERVED_WORDS = Arrays.asList("SQL", "GROUP", "LANGUAGE", "BY", "DECLARE",
			"SELECT", "WHERE", "FROM");
	private final Database database;

	public OracleScriptWriter(Database database) {
		this.database = database;
	}

	public String writeScript() {
		return String.format(SCHEMA_CREATION, database.getSchemaName());
	}

	public String writeOneTable(Table table) {
		StringBuilder tableCreateScript = new StringBuilder("CREATE TABLE ");
		tableCreateScript.append(cleanName(table.getName())).append(" (\n");
		if ( table.getColumns().isEmpty() )
			throw new OracleScriptWriterException("Cannot write a table with no columns");
		for ( Column column : table.getColumns() ) {
			tableCreateScript.append("\t").append(cleanName(column.getName())).append(" ");
			tableCreateScript.append(column.getDataType().getOracleType());

			tableCreateScript.append("(").append(column.getLength()).append(")\n");
		}
		tableCreateScript.append(");\n");
		return tableCreateScript.toString();
	}

	public String cleanName(String name) {
		if ( RESERVED_WORDS.contains(name.toUpperCase()) )
			return name.toUpperCase() + "_";
		if ( name.equals(name.toUpperCase()) )
			return name;
		name = name.replaceAll(" ", "_");
		name = name.replaceAll("[\\(\\)\\\\/#]", "");
		List<Boolean> upperCase = new ArrayList<>(name.length());
		for ( int i = 0; i < name.length(); i++ ) {
			upperCase.add(Character.isUpperCase(name.charAt(i)));
		}
		for ( int index = 1; index < name.length(); index++ ) {
			if ( upperCase.get(index) && index < name.length() - 1 ) {
				if (index + 1 < name.length() - 1 && upperCase.get(index + 1))
					continue;
				List<Boolean> uppers = upperCase.subList(index-1, upperCase.size()-1);
				if (name.charAt(index-1) != '_' && !isOnlyUpperCase(uppers)) {
					name = name.substring(0, index) + "_" + name.substring(index);
					upperCase.add(index + 1, false);
				}
			}
		}
		return name.toUpperCase();
	}

	private boolean isOnlyUpperCase(List<Boolean> uppers) {
		for ( Boolean bool : uppers ) {
			if ( !bool )
				return false;
		}
		return true;
	}
}
