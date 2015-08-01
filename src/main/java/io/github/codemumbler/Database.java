package io.github.codemumbler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * A Java representation of the Access database.
 */
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
    List<Table> orderedTables = new LinkedList<>();
    for (Table table : tables) {
      int parentTableIndex = orderedTables.size();
      if (!table.getForeignKeys().isEmpty()) {
        for (ForeignKey key : table.getForeignKeys()) {
          if (orderedTables.contains(key.getParentTable()) && !orderedTables.contains(table)) {
            parentTableIndex = Math.max(0, orderedTables.indexOf(key.getParentTable()) + 1);
          }
        }
      }
      for (Table orderedTable : orderedTables) {
        if (!orderedTable.getForeignKeys().isEmpty()) {
          for (ForeignKey key : orderedTable.getForeignKeys()) {
            if (table.equals(key.getParentTable()) && !orderedTables.contains(table)) {
              parentTableIndex = Math.max(0, orderedTables.indexOf(key.getParentTable()));
            }
          }
        }
      }
      orderedTables.add(parentTableIndex, table);
    }
    return orderedTables;
  }

  public void addTable(Table table) {
    tables.add(table);
  }

  public Table getTable(String tableName) {
    for (Table table : tables) {
      if (table.getName().equals(tableName)) {
        return table;
      }
    }
    return null;
  }
}
