package io.github.codemumbler;

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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ForeignKey that = (ForeignKey) o;

		if (parentTable != null ? !parentTable.equals(that.parentTable) : that.parentTable != null) return false;
		if (parentColumn != null ? !parentColumn.equals(that.parentColumn) : that.parentColumn != null) return false;
		return !(childColumn != null ? !childColumn.equals(that.childColumn) : that.childColumn != null);

	}

	@Override
	public int hashCode() {
		int result = parentTable != null ? parentTable.hashCode() : 0;
		result = 31 * result + (parentColumn != null ? parentColumn.hashCode() : 0);
		result = 31 * result + (childColumn != null ? childColumn.hashCode() : 0);
		return result;
	}
}
