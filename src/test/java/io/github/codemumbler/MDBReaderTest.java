package io.github.codemumbler;

import io.github.codemumbler.datatype.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Date;
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
		Assert.assertEquals(LongDataType.class, column.getDataType().getClass());
	}

	@Test
	public void textDataType() {
		setUpSimpleDatabase();
		Column column = getTableColumn(LABEL_COLUMN);
		Assert.assertEquals(Text.class, column.getDataType().getClass());
	}

	@Test
	public void memoDataType() {
		setUpSimpleDatabase();
		Column column = getTableColumn(SIMPLE_VALUES_TABLE, 1);
		Assert.assertEquals(Memo.class, column.getDataType().getClass());
	}

	@Test
	public void integerDataType() {
		setUpSimpleDatabase();
		Column column = getTableColumn(SIMPLE_VALUES_TABLE, 6);
		Assert.assertEquals(IntegerDataType.class, column.getDataType().getClass());
	}

	@Test
	public void dateTimeDataType() {
		setUpSimpleDatabase();
		Column column = getTableColumn(SIMPLE_VALUES_TABLE, 2);
		Assert.assertEquals(DateTime.class, column.getDataType().getClass());
	}

	@Test
	public void doubleDataType() {
		setUpSimpleDatabase();
		Column column = getTableColumn(SIMPLE_VALUES_TABLE, 7);
		Assert.assertEquals(DoubleDataType.class, column.getDataType().getClass());
	}

	@Test
	public void booleanDataType() {
		setUpSimpleDatabase();
		Column column = getTableColumn(SIMPLE_VALUES_TABLE, 4);
		Assert.assertEquals(BooleanDataType.class, column.getDataType().getClass());
	}

	@Test
	public void booleanLength() {
		setUpSimpleDatabase();
		Column column = getTableColumn(SIMPLE_VALUES_TABLE, 4);
		Assert.assertEquals(1, column.getLength());
	}

	@Test
	public void precision() {
		setUpSimpleDatabase();
		Column column = getTableColumn(SIMPLE_VALUES_TABLE, 7);
		Assert.assertEquals(4, column.getPrecision());
	}

	@Test
	public void required() {
		setUpSimpleDatabase();
		Assert.assertTrue(getTableColumn(LABEL_COLUMN).isRequired());
	}

	@Test
	public void primaryKeyIsRequiredColumn() {
		setUpSimpleDatabase();
		Assert.assertTrue(getTableColumn(ID_COLUMN).isRequired());
	}

	@Test
	public void autoPrecision() {
		setUpMDBReader("badExamples.accdb");
		Column column = getTableColumn(0, 2);
		Assert.assertEquals(5, column.getPrecision());
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

	@Test
	public void simpleDataCount() {
		setUpSimpleDatabase();
		Assert.assertEquals(1, table.getRows().size());
	}

	@Test
	public void simpleDataColumns() {
		setUpSimpleDatabase();
		Assert.assertEquals("ID,label,description", columnToString(table.getRows().get(0).getColumns()));
	}

	@Test
	public void simpleData() {
		setUpSimpleDatabase();
		Assert.assertEquals("1,label1,description1", rowDataToString(table.getRows().get(0)));
	}

	@Test
	public void readDateValue() {
		setUpMDBReader(SIMPLE_DATABASE_FILE);
		table = database.getTables().get(SIMPLE_VALUES_TABLE);
		Row data = table.getRows().get(0);
		Column column = getTableColumn(SIMPLE_VALUES_TABLE, 2);
		Date date = (Date) data.get(column);
		Assert.assertEquals(1427860800000L, date.getTime());
	}

	@Test
	public void foreignKeysCount() {
		setUpSimpleDatabase();
		Assert.assertEquals(2, database.getTables().get(SIMPLE_VALUES_TABLE).getForeignKeys().size());
	}

	@Test
	public void lookupForeignKeyParentTable() {
		setUpSimpleDatabase();
		Assert.assertEquals("SimpleTable", simpleValesForeignKeys(0).getParentTable().getName());
	}

	@Test
	public void lookupForeignKeyParentColumn() {
		setUpSimpleDatabase();
		Assert.assertEquals("ID", simpleValesForeignKeys(0).getParentColumn().getName());
	}

	@Test
	public void lookupForeignKeyChildColumn() {
		setUpSimpleDatabase();
		Assert.assertEquals("label lookup", simpleValesForeignKeys(0).getChildColumn().getName());
	}

	@Test
	public void lookupChildColumnMarkedAsForeignKey() {
		setUpSimpleDatabase();
		Assert.assertTrue(simpleValesForeignKeys(0).getChildColumn().isForeignKey());
	}

	@Test
	public void relationshipForeignKeyParentTable() {
		setUpSimpleDatabase();
		Assert.assertEquals("SimpleTable", simpleValesForeignKeys(1).getParentTable().getName());
	}

	@Test
	public void relationshipForeignKeyParentColumn() {
		setUpSimpleDatabase();
		Assert.assertEquals("ID", simpleValesForeignKeys(1).getParentColumn().getName());
	}

	@Test
	public void relationshipForeignKeyChildColumn() {
		setUpSimpleDatabase();
		Assert.assertEquals("foreign key", simpleValesForeignKeys(1).getChildColumn().getName());
	}

	@Test
	public void childColumnMarkedAsForeignKey() {
		setUpSimpleDatabase();
		Assert.assertTrue(simpleValesForeignKeys(1).getChildColumn().isForeignKey());
	}

	@Test
	public void foreignKeyOnNonPrimaryParentColumnDoesNotCreate() {
		setUpMDBReader("badExamples.accdb");
		Assert.assertEquals(0, database.getTable("notAGoodForeignKey").getForeignKeys().size());
	}

	@Test
	public void maxPrimaryKeyValueForBuildingSequence() {
		setUpSimpleDatabase();
		Assert.assertEquals(2, database.getTable("SimpleTable").getNextValue());
	}

	private ForeignKey simpleValesForeignKeys(int index) {
		return database.getTables().get(SIMPLE_VALUES_TABLE).getForeignKeys().get(index);
	}

	private String rowDataToString(Row row) {
		String data = "";
		for ( Column column : row.getColumns() ) {
			data += row.get(column) + ",";
		}
		return data.substring(0, data.length() - 1);
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
			e.printStackTrace();
			Assert.fail("Failed to initialized access database");
		}
	}
}
