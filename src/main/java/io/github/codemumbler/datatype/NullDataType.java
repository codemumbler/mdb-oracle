package io.github.codemumbler.datatype;

import io.github.codemumbler.OracleScriptWriterException;

public class NullDataType extends DataType {

	@Override
	public String getOracleType() {
		throw new OracleScriptWriterException("Unknown dataType in use.");
	}

	@Override
	public String writeDataValue(Object value) {
		throw new OracleScriptWriterException("Cannot create value for unknown dataType");
	}
}
