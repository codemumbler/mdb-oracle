package io.github.codemumbler;

import org.junit.Assert;
import org.junit.Test;

public class DatabaseTest {

	@Test
	public void getTableNotFound() {
		Database database = new Database();
		Assert.assertNull(database.getTable("notFound"));
	}

	@Test
	public void getTableByName() {
		Database database = new Database();
		Table table1 = new Table();
		table1.setName("TestTable1");
		database.addTable(table1);
		Table table2 = new Table();
		table2.setName("TestTable2");
		database.addTable(table2);
		Assert.assertEquals(table1, database.getTable("TestTable1"));
	}
}