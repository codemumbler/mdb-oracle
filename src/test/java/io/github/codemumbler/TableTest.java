package io.github.codemumbler;

import org.junit.Assert;
import org.junit.Test;

public class TableTest {

  @Test
  public void findColumnNotFound() {
    Table table = new Table();
    Assert.assertNull(table.getColumn("notFound"));
  }

  @Test
  public void findColumnByName() {
    Table table = new Table();
    Column column = new Column();
    column.setName("column1");
    table.addColumn(column);
    Assert.assertEquals(column, table.getColumn(column.getName()));
  }
}
