package io.github.codemumbler;

import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Index;
import com.healthmarketscience.jackcess.PropertyMap;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MDBReader {

	private static final int TEXT = 12;
	private static final int LONG_INTEGER = 4;
	private static final int MEMO = -1;
	private static final int INTEGER = 5;
	public static final int DATE_TIME = 93;
	private static final int DOUBLE = 8;
	private static final int BOOLEAN = 16;

	private Database database;
	private com.healthmarketscience.jackcess.Database jackcessDatabase;

	public MDBReader(File mdbFile) throws IOException {
		if ( mdbFile == null )
			throw new IllegalArgumentException();
		this.jackcessDatabase = DatabaseBuilder.open(mdbFile);
		database = new Database();
		String schemaName = mdbFile.getName();
		schemaName = schemaName.replace(".mdb", "").replace(".accdb", "");
		database.setSchemaName(schemaName);
	}

	public Database loadDatabase() {
		try {
			readTables();
		} catch (SQLException | IOException ignored) {
		}
		return database;
	}

	private void readTables() throws IOException, SQLException {
		List<Table> tables = new ArrayList<>(jackcessDatabase.getTableNames().size());
		for (String tableName : jackcessDatabase.getTableNames()) {
			Table table = new Table();
			table.setName(tableName);
			table.setColumns(readTableColumns(tableName));
			tables.add(table);
		}
		database.setTables(tables);
	}

	private List<Column> readTableColumns(String tableName) throws IOException, SQLException {
		List<com.healthmarketscience.jackcess.Column> originalColumns = (List<com.healthmarketscience.jackcess.Column>)
				jackcessDatabase.getTable(tableName).getColumns();
		List<Column> columns = new ArrayList<>(originalColumns.size());
		for ( com.healthmarketscience.jackcess.Column originalColumn : originalColumns ) {
			Column column = new Column();
			column.setName(originalColumn.getName());
			column.setDataType(readDataType(originalColumn));
			column.setLength(readLength(originalColumn));
			column.setPrimary(isPrimaryColumn(tableName, originalColumn));
			column.setAutoIncrement(originalColumn.isAutoNumber());
			if ( column.getDataType() == DataType.DOUBLE )
				column.setPrecision(precision(originalColumn));
			columns.add(column);
		}
		return columns;
	}

	private int precision(com.healthmarketscience.jackcess.Column originalColumn) throws IOException {
		int precision = 5;
		if ( readColumnProperty(originalColumn, "DecimalPlaces") != null ) {
			precision = (Byte) readColumnProperty(originalColumn, "DecimalPlaces");
			if (precision <= 0)
				precision = 5;
		}
		return precision;
	}

	private Object readColumnProperty(com.healthmarketscience.jackcess.Column column, String propertyName)
			throws IOException {
		PropertyMap.Property property = column.getProperties().get(propertyName);
		return property.getValue();
	}

	private int readLength(com.healthmarketscience.jackcess.Column originalColumn) throws SQLException {
		switch ( originalColumn.getSQLType() ) {
			case DOUBLE:
			case LONG_INTEGER:
				return 9;
			default:
				return originalColumn.getLength();
		}
	}

	private boolean isPrimaryColumn(String tableName, com.healthmarketscience.jackcess.Column originalColumn)
			throws IOException {
		for ( Index index : jackcessDatabase.getTable(tableName).getIndexes() ) {
			if (index.isPrimaryKey()) {
				for (Index.Column indexColumn : index.getColumns()) {
					if (indexColumn.getName().equals(originalColumn.getName())) {
						return true;
					}
				}
			}
		}
		boolean hasPrimaryKey = false;
		for ( Index index : jackcessDatabase.getTable(tableName).getIndexes() ) {
			if (index.isPrimaryKey()) {
				hasPrimaryKey = true;
			}
		}
		return originalColumn.isAutoNumber() && !hasPrimaryKey;
	}

	private DataType readDataType(com.healthmarketscience.jackcess.Column originalColumn) throws SQLException {
		switch ( originalColumn.getSQLType() ) {
			case TEXT:
				return DataType.TEXT;
			case INTEGER:
				return DataType.INTEGER;
			case LONG_INTEGER:
				return DataType.LONG;
			case MEMO:
				return DataType.MEMO;
			case DATE_TIME:
				return DataType.DATE_TIME;
			case DOUBLE:
				return DataType.DOUBLE;
			case BOOLEAN:
				return DataType.BOOLEAN;
			default:
				return null;
		}
	}
}
