package io.github.codemumbler;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class MDBReaderTest {

	private static final String SIMPLE_DATABASE_FILE = "simple.accdb";
	private static final int SIMPLE_TABLE = 0;
	private static final int ID_COLUMN = 0;
	private static final int LABEL_COLUMN = 1;

	private MDBReader reader;
	private Database database;
	private Table table;

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
		setUpSimpleDatabase();
		Assert.assertEquals("SimpleTable", table.getName());
	}

	private void setUpSimpleDatabase() {
		setUpMDBReader(SIMPLE_DATABASE_FILE);
		table = database.getTables().get(SIMPLE_TABLE);
	}

	@Test
	public void columnCount() {
		setUpSimpleDatabase();
		Assert.assertEquals(3, table.getColumns().size());
	}

	@Test
	public void columnNames() {
		setUpSimpleDatabase();
		Assert.assertEquals("ID,label,description", columnToString(table.getColumns()));
	}

	@Test
	public void longIntegerDataType() {
		setUpSimpleDatabase();
		Column column = table.getColumns().get(ID_COLUMN);
		Assert.assertEquals(DataType.LONG, column.getDataType());
	}

	@Test
	public void textDataType() {
		setUpSimpleDatabase();
		Column column = table.getColumns().get(LABEL_COLUMN);
		Assert.assertEquals(DataType.TEXT, column.getDataType());
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
