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
	private static final int SIMPLE_VALUES_TABLE = 1;

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
		Column column = getTableColumn(ID_COLUMN);
		Assert.assertEquals(DataType.LONG, column.getDataType());
	}

	@Test
	public void textDataType() {
		setUpSimpleDatabase();
		Column column = getTableColumn(LABEL_COLUMN);
		Assert.assertEquals(DataType.TEXT, column.getDataType());
	}

	@Test
	public void memoDataType() {
		setUpSimpleDatabase();
		Column column = getTableColumn(SIMPLE_VALUES_TABLE, 1);
		Assert.assertEquals(DataType.MEMO, column.getDataType());
	}

	@Test
	public void integerDataType() {
		setUpSimpleDatabase();
		Column column = getTableColumn(SIMPLE_VALUES_TABLE, 6);
		Assert.assertEquals(DataType.INTEGER, column.getDataType());
	}

	@Test
	public void dateTimeDataType() {
		setUpSimpleDatabase();
		Column column = getTableColumn(SIMPLE_VALUES_TABLE, 2);
		Assert.assertEquals(DataType.DATE_TIME, column.getDataType());
	}

	@Test
	public void doubleDataType() {
		setUpSimpleDatabase();
		Column column = getTableColumn(SIMPLE_VALUES_TABLE, 7);
		Assert.assertEquals(DataType.DOUBLE, column.getDataType());
	}

	@Test
	public void booleanDataType() {
		setUpSimpleDatabase();
		Column column = getTableColumn(SIMPLE_VALUES_TABLE, 4);
		Assert.assertEquals(DataType.BOOLEAN, column.getDataType());
	}

	@Test
	public void memoLength() {
		setUpSimpleDatabase();
		Assert.assertEquals(0, getTableColumn(SIMPLE_VALUES_TABLE, 1).getLength());
	}

	@Test
	public void primaryColumn() {
		setUpSimpleDatabase();
		Column column = getTableColumn(ID_COLUMN);
		Assert.assertTrue(column.isPrimary());
	}

	@Test
	public void notPrimaryColumn() {
		setUpSimpleDatabase();
		Column column = getTableColumn(LABEL_COLUMN);
		Assert.assertFalse(column.isPrimary());
	}

	@Test
	public void autoIncrements() {
		setUpSimpleDatabase();
		Column column = getTableColumn(ID_COLUMN);
		Assert.assertTrue(column.isAutoIncrement());
	}

	@Test
	public void nonAutoIncrement() {
		setUpSimpleDatabase();
		Column column = getTableColumn(LABEL_COLUMN);
		Assert.assertFalse(column.isAutoIncrement());
	}

	@Test
	public void autoIncrementingColumnMadeIntoPrimaryWhenNonExists() {
		setUpMDBReader("badExamples.accdb");
		table = database.getTables().get(0);
		Column column = getTableColumn(0, 0);
		Assert.assertTrue(column.isPrimary());
	}

	@Test
	public void longIntegerColumnLength() {
		setUpSimpleDatabase();
		Column column = getTableColumn(SIMPLE_TABLE, ID_COLUMN);
		Assert.assertEquals(9, column.getLength());
	}

	@Test
	public void textColumnLength() {
		setUpSimpleDatabase();
		Column column = getTableColumn(LABEL_COLUMN);
		Assert.assertEquals(510, column.getLength());
	}

	private Column getTableColumn(int columnIndex) {
		return database.getTables().get(SIMPLE_TABLE).getColumns().get(columnIndex);
	}

	private Column getTableColumn(int tableIndex, int columnIndex) {
		return database.getTables().get(tableIndex).getColumns().get(columnIndex);
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
