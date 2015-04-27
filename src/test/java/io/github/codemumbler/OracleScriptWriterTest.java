package io.github.codemumbler;

import org.junit.Assert;
import org.junit.Test;

public class OracleScriptWriterTest {

	@Test
	public void convertEmptySchema() {
		Database database = new Database();
		database.setSchemaName("EmptySchema");
		OracleScriptWriter writer = new OracleScriptWriter(database);
		Assert.assertEquals("--CREATE USER EmptySchema IDENTIFIED BY password2Change;\n" +
				"-- GRANT CONNECT, RESOURCE, CREATE SESSION, CREATE TABLE, CREATE VIEW,\n" +
				"-- \tCREATE PROCEDURE,CREATE SYNONYM, CREATE SEQUENCE, CREATE TRIGGER TO EmptySchema;\n",
				writer.writeScript());
	}
}
