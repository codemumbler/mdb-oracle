package io.github.codemumbler;

import com.healthmarketscience.jackcess.DatabaseBuilder;
import com.healthmarketscience.jackcess.Index;
import com.healthmarketscience.jackcess.PropertyMap;
import com.healthmarketscience.jackcess.Relationship;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MDBReader {

	private static final int TEXT = 12;
	private static final int LONG_INTEGER = 4;
	private static final int MEMO = -1;
	private static final int INTEGER = 5;
	public static final int DATE_TIME = 93;
	private static final int DOUBLE = 8;
	private static final int BOOLEAN = 16;
	public static final int DEFAULT_PRECISION = 5;

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
			buildForeignKeys();
		} catch (SQLException | IOException ignored) {
		}
		return database;
	}

	private void buildForeignKeys() throws IOException {
		for (String tableName : jackcessDatabase.getTableNames()) {
			List<com.healthmarketscience.jackcess.Column> originalColumns = (List<com.healthmarketscience.jackcess.Column>)
					jackcessDatabase.getTable(tableName).getColumns();
			for (com.healthmarketscience.jackcess.Column column : originalColumns ) {
				String lookupColumn = (String) readColumnProperty(column, "RowSourceType", "");
				if ( lookupColumn.equals("Table/Query") ) {
					ForeignKey foreignKey = new ForeignKey();
					String columns = parseSQL(column, "SELECT (.*) FROM.*");
					String parentTable = parseSQL(column, ".*FROM (.*?);");
					columns = columns.replaceAll("\\[" + parentTable + "\\]", "");
					columns = columns.replaceAll("\\[", "").replaceAll("\\]", "").replaceAll("\\.", "");
					int boundColumn = (Short) readColumnProperty(column, "BoundColumn", 1);
					foreignKey.setParentTable(database.getTable(parentTable));
					foreignKey.setParentColumn(foreignKey.getParentTable().getColumn(columns.split(",")[boundColumn - 1]));
					foreignKey.setChildColumn(database.getTable(tableName).getColumn(column.getName()));
					database.getTable(tableName).addForeignKey(foreignKey);
				}
			}
		}
		for (Relationship relationship : jackcessDatabase.getRelationships()) {
			ForeignKey foreignKey = new ForeignKey();
			foreignKey.setParentTable(database.getTable(relationship.getFromTable().getName()));
			foreignKey.setParentColumn(foreignKey.getParentTable().getColumn(relationship.getFromColumns().get(0).getName()));
			foreignKey.setChildColumn(database.getTable(relationship.getToTable().getName()).getColumn(relationship.getToColumns().get(0).getName()));
			if ( foreignKey.getParentColumn().isPrimary() )
				database.getTable(relationship.getToTable().getName()).addForeignKey(foreignKey);
		}
	}

	private String parseSQL(com.healthmarketscience.jackcess.Column column, String regex) throws IOException {
		String sql = (String) readColumnProperty(column, "RowSource", "");
		Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(sql);
		if ( matcher.find() )
			return matcher.group(1);
		return null;
	}

	private void readTables() throws IOException, SQLException {
		for (String tableName : jackcessDatabase.getTableNames()) {
			Table table = new Table();
			table.setName(tableName);
			table.addAllColumns(readTableColumns(tableName));
			table.setRows(readTableData(table));
			database.addTable(table);
		}
	}

	private List<Row> readTableData(Table table) throws IOException {
		com.healthmarketscience.jackcess.Table sourceTable = jackcessDatabase.getTable(table.getName());
		List<Row> rows = new ArrayList<>(sourceTable.getRowCount());
		for ( com.healthmarketscience.jackcess.Row sourceRow : sourceTable ) {
			Row row = new Row(table);
			for ( Column column : table.getColumns() ) {
				row.add(column, sourceRow.get(column.getName()));
			}
			rows.add(row);
		}
		return rows;
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
			column.setRequired(column.isPrimary() || (Boolean) readColumnProperty(originalColumn, "Required", false));
			if ( column.getDataType() == DataType.DOUBLE )
				column.setPrecision(precision(originalColumn));
			columns.add(column);
		}
		return columns;
	}

	private int precision(com.healthmarketscience.jackcess.Column originalColumn) throws IOException {
		int precision = (Byte) readColumnProperty(originalColumn, "DecimalPlaces", DEFAULT_PRECISION);
		if (precision <= 0)
			precision = DEFAULT_PRECISION;
		return precision;
	}

	private Object readColumnProperty(com.healthmarketscience.jackcess.Column column, String propertyName,
									  Object defaultValue) throws IOException {
		PropertyMap.Property property = column.getProperties().get(propertyName);
		if ( property != null )
			return property.getValue();
		return defaultValue;
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
