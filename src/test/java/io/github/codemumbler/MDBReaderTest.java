package io.github.codemumbler;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class MDBReaderTest {

	private static final String SIMPLE_DATABASE_FILE = "simple.accdb";
	private MDBReader reader;

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
		Database database = reader.loadDatabase();
		Assert.assertEquals("access_2000", database.getSchemaName());
	}

	@Test
	public void schemaName() {
		setUpMDBReader(SIMPLE_DATABASE_FILE);
		Database database = reader.loadDatabase();
		Assert.assertEquals("simple", database.getSchemaName());
	}

	@Test
	public void table() {
		setUpMDBReader(SIMPLE_DATABASE_FILE);
		Database database = reader.loadDatabase();
		Assert.assertEquals(1, database.getTables().size());
	}

	private void setUpMDBReader(String mdbFileName) {
		try {
			reader = new MDBReader(new File(ClassLoader.getSystemResource(mdbFileName).toURI()));
		} catch (Exception e) {
			Assert.fail("Failed to initialized access database");
		}
	}
}
