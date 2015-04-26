package io.github.codemumbler;

import com.healthmarketscience.jackcess.DatabaseBuilder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MDBReader {

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
		} catch (IOException ignored) {
		}
		return database;
	}

	private void readTables() throws IOException {
		List<Table> tables = new ArrayList<>(jackcessDatabase.getTableNames().size());
		for (String tableName : jackcessDatabase.getTableNames()) {
			Table table = new Table();
			table.setName(tableName);
			table.setColumns(readTableColumns(tableName));
			tables.add(table);
		}
		database.setTables(tables);
	}

	private List<Column> readTableColumns(String tableName) throws IOException {
		List<com.healthmarketscience.jackcess.Column> originalColumns = (List<com.healthmarketscience.jackcess.Column>)
				jackcessDatabase.getTable(tableName).getColumns();
		List<Column> columns = new ArrayList<>(originalColumns.size());
		for ( com.healthmarketscience.jackcess.Column originalColumn : originalColumns ) {
			Column column = new Column();
			column.setName(originalColumn.getName());
			columns.add(column);
		}
		return columns;
	}
}
