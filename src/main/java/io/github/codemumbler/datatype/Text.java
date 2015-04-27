package io.github.codemumbler.datatype;

public class Text extends DataType {

	@Override
	public String getOracleType() {
		return "VARCHAR2";
	}

	public String writeValue(Object value) {
		String strValue = (String) value;
		strValue = strValue.replaceAll("'", "''");
		return "'" + strValue + "'";
	}
}
