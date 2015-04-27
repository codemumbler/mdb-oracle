package io.github.codemumbler;

import java.util.ArrayList;
import java.util.List;

public class Table {

	private String name;
	private List<Column> columns = new ArrayList<>();
	private List<Row> rows;
	private List<ForeignKey> foreignKeys = new ArrayList<>();
	private long nextValue;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Column> getColumns() {
		return columns;
	}

	public void addColumn(Column column) {
		this.columns.add(column);
	}

	public List<Row> getRows() {
		return rows;
	}

	public void setRows(List<Row> rows) {
		this.rows = rows;
	}

	public List<ForeignKey> getForeignKeys() {
		return foreignKeys;
	}

	public void addForeignKey(ForeignKey foreignKey) {
		foreignKeys.add(foreignKey);
	}

	public Column getColumn(String columnName) {
		for ( Column column : columns ) {
			if ( column.getName().equals(columnName) )
				return column;
		}
		return null;
	}

	public void addAllColumns(List<Column> columns) {
		this.columns.addAll(columns);
	}

	public long getNextValue() {
		return nextValue;
	}

	public void setNextValue(long nextValue) {
		this.nextValue = nextValue;
	}
}
