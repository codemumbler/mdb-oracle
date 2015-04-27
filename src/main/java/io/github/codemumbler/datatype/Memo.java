package io.github.codemumbler.datatype;

public class Memo extends Text {

	@Override
	public String getOracleType() {
		return "CLOB";
	}

	@Override
	public boolean hasLength() {
		return false;
	}

	@Override
	public boolean isInsertable() {
		return false;
	}
}
