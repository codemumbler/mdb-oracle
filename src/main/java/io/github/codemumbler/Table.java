package io.github.codemumbler;

import io.github.codemumbler.datatype.NumberDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Table {

	private String name;
	private List<Column> columns = new ArrayList<>();
	private List<Row> rows = new ArrayList<>();
	private List<ForeignKey> foreignKeys = new ArrayList<>();
	private long nextValue;
	private Map<Column, String> indexedValues = new HashMap<>();

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

	public void addRow(Row row) {
		rows.add(row);
	}

	public Column getPrimaryColumn() {
		for ( Column column : getColumns() )
			if ( column.isPrimary() )
				return column;
		return null;
	}

	public boolean hasPrimaryKey() {
		return getPrimaryColumn() != null;
	}

	public boolean parentTablesHaveForeignKeyValue(Row row) {
		for ( Column column : row.getColumns() ) {
			if ( column.isForeignKey() ) {
				for ( ForeignKey foreignKey : foreignKeys ) {
					if ( foreignKey.getChildColumn().equals(column) && foreignKey.getParentTable().keyExists(foreignKey.getParentColumn(), row.get(column)) ) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean keyExists(Column parentColumn, Object value) {
		if ( indexedValues.get(parentColumn) == null ) {
			StringBuilder values = new StringBuilder(" 0,");
			for (Row row : getRows()) {
				values.append(" ").append(row.get(parentColumn)).append(",");
			}
			indexedValues.put(parentColumn, values.toString());
		}
		return indexedValues.get(parentColumn).contains(" " + value + ",");
	}
}
