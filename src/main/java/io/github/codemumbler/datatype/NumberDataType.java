package io.github.codemumbler.datatype;

public class NumberDataType extends DataType {

	@Override
	public String getOracleType() {
		return "NUMBER";
	}

	@Override
	public String writeValue(Object value) {
		return value.toString();
	}
}
