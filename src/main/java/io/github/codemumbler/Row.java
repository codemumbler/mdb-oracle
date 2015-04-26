package io.github.codemumbler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Row {

	private Table table;
	private Map<Column, Object> valueMap = new HashMap<>();

	public Row(Table table) {
		this.table = table;
	}

	public List<Column> getColumns() {
		return table.getColumns();
	}

	public Object get(Column column) {
		return valueMap.get(column);
	}

	public void add(Column column, Object value) {
		valueMap.put(column, value);
	}
}
