package io.github.codemumbler;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MDBReaderTest {

	private static final String SIMPLE_DATABASE_FILE = "simple.accdb";
	public static final int SIMPLE_TABLE = 0;
	private MDBReader reader;
	private Database database;

	@Test(expected = IllegalArgumentException.class)
	public void noMDBFileSpecified() throws IOException {
		new MDBReader(null);
	}

	@Test(expected = IOException.class)
	public void nonExistentFile() throws IOException {
		new MDBReader(new File("nonExistent.mdb"));
	}

	@Test
	public void emptyDatabaseIsNotNull() {
		setUpMDBReader("empty.accdb");
		Assert.assertNotNull(reader.loadDatabase());
	}

	@Test
	public void schemaName_mdbFile() {
		setUpMDBReader("access_2000.mdb");
		Assert.assertEquals("access_2000", database.getSchemaName());
	}

	@Test
	public void schemaName() {
		setUpMDBReader(SIMPLE_DATABASE_FILE);
		Assert.assertEquals("simple", database.getSchemaName());
	}

	@Test
	public void table() {
		setUpMDBReader(SIMPLE_DATABASE_FILE);
		Assert.assertEquals(1, database.getTables().size());
	}

	@Test
	public void tableName() {
		setUpMDBReader(SIMPLE_DATABASE_FILE);
		Table table = database.getTables().get(SIMPLE_TABLE);
		Assert.assertEquals("SimpleTable", table.getName());
	}

	@Test
	public void columnCount() {
		setUpMDBReader(SIMPLE_DATABASE_FILE);
		Table table = database.getTables().get(SIMPLE_TABLE);
		Assert.assertEquals(3, table.getColumns().size());
	}

	@Test
	public void columnNames() {
		setUpMDBReader(SIMPLE_DATABASE_FILE);
		Table table = database.getTables().get(SIMPLE_TABLE);
		Assert.assertEquals("ID,label,description", columnToString(table.getColumns()));
	}

	private String columnToString(List<Column> columns) {
		String names = "";
		for (Column column : columns){
			names += column.getName() + ",";
		}
		return names.substring(0, names.length() - 1);
	}

	private void setUpMDBReader(String mdbFileName) {
		try {
			reader = new MDBReader(new File(ClassLoader.getSystemResource(mdbFileName).toURI()));
			database = reader.loadDatabase();
		} catch (Exception e) {
			Assert.fail("Failed to initialized access database");
		}
	}
}
