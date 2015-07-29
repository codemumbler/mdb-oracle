package io.github.codemumbler;

import io.github.codemumbler.datatype.DataType;
import io.github.codemumbler.datatype.IntegerDataType;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DatabaseTest {

  private Database database;

  @Before
  public void setUp() {
    database = new Database();
  }

  @Test
  public void getTableNotFound() {
    Assert.assertNull(database.getTable("notFound"));
  }

  @Test
  public void getTableByName() {
    Table table1 = addTable("TestTable1");
    addTable("TestTable2");
    Assert.assertEquals(table1, database.getTable("TestTable1"));
  }

  private Table addTable(String testTable1) {
    Table table1 = new Table();
    table1.setName(testTable1);
    database.addTable(table1);
    return table1;
  }

  @Test
  public void getTablesReturnsOrderedList() {
    Table parentTable = addTable("PARENT_TABLE");
    Column id = addColumnToTable(parentTable, "ID", new IntegerDataType(), 5);
    id.setPrimary(true);
    Row data = new Row(parentTable);
    data.add(id, 1);
    parentTable.addRow(data);

    Table childTable = addTable("CHILD_TABLE");
    Column childColumn = addColumnToTable(childTable, "FOREIGN_ID", new IntegerDataType(), 5);
    data = new Row(childTable);
    data.add(childColumn, 1);
    childTable.addRow(data);
    childColumn.setRequired(true);
    childColumn.setForeignKey(true);
    addForeignKeyToTable(parentTable, id, childTable, childColumn);

    Assert.assertEquals("[Table{name='PARENT_TABLE'}, Table{name='CHILD_TABLE'}]", database.getTables().toString());
  }

  private Column addColumnToTable(Table table, String name, DataType type, int length) {
    Column column = new Column();
    column.setName(name);
    column.setDataType(type);
    column.setLength(length);
    table.addColumn(column);
    return column;
  }

  private void addForeignKeyToTable(Table parentTable, Column id, Table childTable, Column childColumn) {
    ForeignKey foreignKey = new ForeignKey();
    foreignKey.setChildColumn(childColumn);
    foreignKey.setParentTable(parentTable);
    foreignKey.setParentColumn(id);
    childTable.addForeignKey(foreignKey);
  }
}
