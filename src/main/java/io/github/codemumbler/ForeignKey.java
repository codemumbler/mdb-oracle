package io.github.codemumbler;

import java.lang.management.BufferPoolMXBean;

public class ForeignKey {

	private Table parentTable;
	private Column parentColumn;
	private Column childColumn;

	public Table getParentTable() {
		return parentTable;
	}

	public void setParentTable(Table parentTable) {
		this.parentTable = parentTable;
	}

	public Column getParentColumn() {
		return parentColumn;
	}

	public void setParentColumn(Column parentColumn) {
		this.parentColumn = parentColumn;
	}

	public Column getChildColumn() {
		return childColumn;
	}

	public void setChildColumn(Column childColumn) {
		this.childColumn = childColumn;
	}
}
