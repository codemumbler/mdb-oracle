package io.github.codemumbler.datatype;

public class BooleanDataType extends DataType {

	@Override
	public String getOracleType() {
		return "VARCHAR2";
	}
}
