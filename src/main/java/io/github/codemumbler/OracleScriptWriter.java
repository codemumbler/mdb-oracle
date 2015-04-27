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
	private static final String UNIQUE = "\nCREATE UNIQUE INDEX %1$s_UK%3$d ON %1$s (%2$s);\n";
	private static final String PRIMARY = "\nALTER TABLE %1$s ADD CONSTRAINT %1$s_PK PRIMARY KEY (%2$s) ENABLE;\n";
	private static final String SEQUENCE = "\nCREATE SEQUENCE %s_SEQ MINVALUE 1 MAXVALUE %s INCREMENT BY 1 START WITH %d NOCACHE NOORDER NOCYCLE;\n";
	private static final String TRIGGER = "\nCREATE OR REPLACE TRIGGER %1$s_TRIG\n" +
			"BEFORE INSERT ON %1$s\n" +
			"FOR EACH ROW BEGIN\n" +
			"\tIF :NEW.%2$s IS NULL THEN\n" +
			"\t\tSELECT %1$s_SEQ.nextVal\n" +
			"\t\tINTO :NEW.%2$s\n" +
			"\t\tFROM dual;\n" +
			"\tEND IF;\n" +
			"END;\n" +
			"/\n" +
			"ALTER TRIGGER %1$s_TRIG ENABLE;";
	private static final String INSERTION = "INSERT INTO %s(%s) VALUES (%s);\n";

	private final Database database;

	public OracleScriptWriter(Database database) {
		this.database = database;
	}

	public String writeScript() {
		return String.format(SCHEMA_CREATION, database.getSchemaName());
	}

	public String writeTableScript(Table table) {
		StringBuilder tableCreateScript = new StringBuilder("CREATE TABLE ");
		StringBuilder tableAlterationsScript = new StringBuilder();
		int uniqueIndexNumber = 1;
		String tableName = cleanName(table.getName());
		tableCreateScript.append(tableName).append(" (\n");
		if ( table.getColumns().isEmpty() )
			throw new OracleScriptWriterException("Cannot write a table with no columns");
		for ( Column column : table.getColumns() ) {
			String columnName = cleanName(column.getName());
			tableCreateScript.append("\t").append(columnName).append(" ");
			tableCreateScript.append(column.getDataType().getOracleType());
			if ( column.getDataType().hasLength() ) {
				tableCreateScript.append("(").append(column.getLength());
				if ( column.getDataType().hasPrecision() )
					tableCreateScript.append(",").append(column.getPrecision());
				tableCreateScript.append(")");
			}
			if ( column.isRequired() ) {
				tableCreateScript.append(" NOT NULL");
			}
			tableCreateScript.append(",\n");

			if ( column.isPrimary() ) {
				tableAlterationsScript.append(String.format(UNIQUE, tableName, columnName, uniqueIndexNumber++));
				tableAlterationsScript.append(String.format(PRIMARY, tableName, columnName));
			}
			if ( column.isAutoIncrement() ) {
				String maxSequenceValue = new String(new char[column.getLength()]).replace("\0", "9");
				tableAlterationsScript.append(String.format(SEQUENCE, tableName, maxSequenceValue, table.getNextValue()));
				tableAlterationsScript.append(String.format(TRIGGER, tableName, columnName));
			}
		}
		tableCreateScript = tableCreateScript.deleteCharAt(tableCreateScript.length() - 2);

		tableCreateScript.append(");\n");
		tableCreateScript.append(tableAlterationsScript);
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

	public String writeTableInsertions(Table table) {
		String tableName = cleanName(table.getName());
		String columns = tableColumnsToString(table);
		StringBuilder insertions = new StringBuilder();
		for ( Row row : table.getRows() ) {
			String values = rowValues(row);
			insertions.append(String.format(INSERTION, tableName, columns, values));
		}
		return insertions.toString();
	}

	private String rowValues(Row row) {
		StringBuilder builder = new StringBuilder();
		for ( Column column : row.getColumns() ) {
			if ( row.get(column) == null )
				builder.append("NULL");
			else
				builder.append(column.getDataType().writeValue(row.get(column)));
			builder.append(", ");
		}
		builder.delete(builder.length() - 2, builder.length());
		return builder.toString();
	}

	private String tableColumnsToString(Table table) {
		StringBuilder builder = new StringBuilder();
		for ( Column column : table.getColumns() ) {
			builder.append(cleanName(column.getName())).append(", ");
		}
		builder.delete(builder.length() - 2, builder.length());
		return builder.toString();
	}
}
