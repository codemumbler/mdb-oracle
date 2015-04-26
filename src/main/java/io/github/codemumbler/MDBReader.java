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
		} catch (IOException e) {
		}
		return database;
	}

	private void readTables() throws IOException {
		List<Table> tables = new ArrayList<>(jackcessDatabase.getTableNames().size());
		for (String tableName : jackcessDatabase.getTableNames()) {
			Table table = new Table();
			tables.add(table);
		}
		database.setTables(tables);
	}
}
