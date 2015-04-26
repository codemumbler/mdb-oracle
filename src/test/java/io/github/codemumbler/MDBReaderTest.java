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
	public void tableCount() {
		setUpMDBReader(SIMPLE_DATABASE_FILE);
		Assert.assertEquals(2, database.getTables().size());
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
		Column column = getColumn(ID_COLUMN);
		Assert.assertEquals(DataType.LONG, column.getDataType());
	}

	@Test
	public void textDataType() {
		setUpSimpleDatabase();
		Column column = getColumn(LABEL_COLUMN);
		Assert.assertEquals(DataType.TEXT, column.getDataType());
	}

	@Test
	public void primaryColumn() {
		setUpSimpleDatabase();
		Column column = getColumn(ID_COLUMN);
		Assert.assertTrue(column.isPrimary());
	}

	@Test
	public void notPrimaryColumn() {
		setUpSimpleDatabase();
		Column column = getColumn(LABEL_COLUMN);
		Assert.assertFalse(column.isPrimary());
	}

	@Test
	public void autoIncrementingColumnMadeIntoPrimary() {
		setUpMDBReader("badExamples.accdb");
		Table table = database.getTables().get(0);
		Column column = table.getColumns().get(0);
		Assert.assertTrue(column.isPrimary());
	}

	@Test
	public void longIntegerColumnLength() {
		setUpSimpleDatabase();
		Column column = getColumn(ID_COLUMN);
		Assert.assertEquals(9, column.getLength());
	}

	@Test
	public void textColumnLength() {
		setUpSimpleDatabase();
		Column column = getColumn(LABEL_COLUMN);
		Assert.assertEquals(510, column.getLength());
	}

	private Column getColumn(int columnIndex) {
		return table.getColumns().get(columnIndex);
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
