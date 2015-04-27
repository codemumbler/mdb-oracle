package io.github.codemumbler.datatype;

public class DateTime extends DataType {

	@Override
	public String getOracleType() {
		return "TIMESTAMP";
	}

	@Override
	public boolean hasLength() {
		return false;
	}
}
