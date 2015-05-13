package io.github.codemumbler;

import io.github.codemumbler.datatype.NumberDataType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Use this class to generate Oracle SQL scripts.
 */
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
	private static final String CLOB_INSERTION = "DECLARE\nstr varchar2(32767);\nBEGIN\n\tstr := %s;\n" +
			"UPDATE %s SET %s = str WHERE %s;\nEND;\n/\n";
	private static final String FOREIGN_KEY = "\nALTER TABLE %1$s ADD CONSTRAINT %1$s_FK%2$d FOREIGN KEY (%3$s)\n" +
			"\tREFERENCES %4$s (%5$s) ENABLE;\n";
	private static final Number ZERO = 0;

	private final Database database;

	public OracleScriptWriter(Database database) {
		this.database = database;
	}

	public String writeSchemaScript() {
		return String.format(SCHEMA_CREATION, database.getSchemaName());
	}

	public String writeTableScript(Table table) {
		StringBuilder tableCreateScript = new StringBuilder("CREATE TABLE ");
		StringBuilder tableAlterationsScript = new StringBuilder();
		StringBuilder primaryColumns = new StringBuilder();
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
				primaryColumns.append(columnName).append(", ");
			}
			if ( column.isAutoIncrement() ) {
				String maxSequenceValue = new String(new char[column.getLength()]).replace("\0", "9");
				tableAlterationsScript.append(String.format(SEQUENCE, tableName, maxSequenceValue, table.getNextValue()));
				tableAlterationsScript.append(String.format(TRIGGER, tableName, columnName));
			}
		}
		if ( !primaryColumns.toString().isEmpty() ) {
			primaryColumns.delete(primaryColumns.length() - 2, primaryColumns.length());
			tableAlterationsScript.append(String.format(UNIQUE, tableName, primaryColumns, 1));
			tableAlterationsScript.append(String.format(PRIMARY, tableName, primaryColumns));
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
		return writeTableInsertions(table, false);
	}

	private String writeTableInsertions(Table table, boolean onlyInvalidData) {
		String tableName = cleanName(table.getName());
		String columns = tableColumnsToString(table);
		StringBuilder insertions = new StringBuilder();
		for ( Row row : table.getRows() ) {
			if ( !table.getForeignKeys().isEmpty() && !table.parentTablesHaveForeignKeyValue(row) )
				insertions.append("--");
			else if (onlyInvalidData)
				continue;
			String values = rowValues(row);
			insertions.append(String.format(INSERTION, tableName, columns, values));
			insertions.append(additionalUpdates(row));
		}
		return insertions.toString();
	}

	private String additionalUpdates(Row row) {
		StringBuilder builder = new StringBuilder();

		for ( Column column : row.getColumns() ) {
			if ( column.getDataType().isInsertable() || row.get(column) == null )
				continue;
			Table table = database.getTable(row.getTableName());
			builder.append(String.format(CLOB_INSERTION,
					column.getDataType().writeValue(row.get(column)),
					cleanName(row.getTableName()),
					cleanName(column.getName()),
					buildSetWhereClause(table, row)
			));
		}
		return builder.toString();
	}

	private String buildSetWhereClause(Table table, Row row) {
		if ( table.hasPrimaryKey() )
			return cleanName(table.getPrimaryColumn().getName()) + " = " +
					table.getPrimaryColumn().getDataType().writeValue(row.getPrimaryKeyValue());
		StringBuilder whereClause = new StringBuilder();
		for ( Column column : row.getColumns() ) {
			if ( column.getDataType().isInsertable() )
				whereClause.append(cleanName(column.getName())).append(" = ").append(
						column.getDataType().writeValue(row.get(column))).append(" AND ");
		}
		whereClause.delete(whereClause.length() - 5, whereClause.length());
		return whereClause.toString();
	}

	private String rowValues(Row row) {
		StringBuilder builder = new StringBuilder();
		for ( Column column : row.getColumns() ) {
			if ( !column.getDataType().isInsertable() )
				continue;
			Object value = row.get(column);
			if ( isNullableForeignKey(column, value) )
				builder.append(column.getDataType().writeValue(null));
			else
				builder.append(column.getDataType().writeValue(row.get(column)));
			builder.append(", ");
		}
		builder.delete(builder.length() - 2, builder.length());
		return builder.toString();
	}

	private boolean isNullableForeignKey(Column column, Object value) {
		return column.getDataType() instanceof NumberDataType && !column.isRequired() && column.isForeignKey() && ZERO.equals(value);
	}

	private String tableColumnsToString(Table table) {
		StringBuilder builder = new StringBuilder();
		for ( Column column : table.getColumns() ) {
			if ( column.getDataType().isInsertable() )
			builder.append(cleanName(column.getName())).append(", ");
		}
		builder.delete(builder.length() - 2, builder.length());
		return builder.toString();
	}

	public String writeForeignKey(Table table) {
		StringBuilder builder = new StringBuilder();
		int foreignKeyIndex = 1;
		for ( ForeignKey foreignKey : table.getForeignKeys() ) {
			builder.append(String.format(FOREIGN_KEY,
					cleanName(table.getName()),
					foreignKeyIndex++,
					cleanName(foreignKey.getChildColumn().getName()),
					cleanName(foreignKey.getParentTable().getName()),
					cleanName(foreignKey.getParentColumn().getName())));
		}
		return builder.toString();
	}

	public String writeDDLScript() {
		StringBuilder builder = new StringBuilder();
		builder.append(writeSchemaScript());
		for ( Table table : database.getTables() ) {
			builder.append(writeTableScript(table));
			builder.append(writeForeignKey(table));
		}
		return builder.toString();
	}

	public String writeDatabaseInsertions() {
		StringBuilder builder = new StringBuilder();
		for ( Table table : database.getTables() ) {
			builder.append(writeTableInsertions(table));
		}
		return builder.toString();
	}

	public String writeScript() {
		StringBuilder builder = new StringBuilder();
		builder.append(writeSchemaScript());
		for ( Table table : database.getTables() ) {
			builder.append(writeTableScript(table));
		}
		for ( Table table : database.getTables() ) {
			builder.append(writeTableInsertions(table));
		}
		for ( Table table : database.getTables() ) {
			builder.append(writeForeignKey(table));
		}
		return builder.toString();
	}

	public String writeInvalidData() {
		StringBuilder builder = new StringBuilder();
		for ( Table table : database.getTables() ) {
			builder.append(writeTableInsertions(table, true));
		}
		return builder.toString();
	}
}
