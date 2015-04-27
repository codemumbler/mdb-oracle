package io.github.codemumbler.datatype;

public class Memo extends DataType {

	@Override
	public String getOracleType() {
		return "CLOB";
	}

	@Override
	public boolean hasLength() {
		return false;
	}
}
