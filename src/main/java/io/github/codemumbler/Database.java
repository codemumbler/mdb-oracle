package io.github.codemumbler;

import java.util.ArrayList;
import java.util.List;

public class Database {

	private String schemaName;
	private List<Table> tables = new ArrayList<>();

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public List<Table> getTables() {
		return tables;
	}

	public void addTable(Table table) {
		tables.add(table);
	}

	public Table getTable(String tableName) {
		for ( Table table : tables ) {
			if ( table.getName().equals(tableName) ) {
				return table;
			}
		}
		return null;
	}
}
