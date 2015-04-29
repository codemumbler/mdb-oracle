package io.github.codemumbler.datatype;

public class Text extends DataType {

	@Override
	public String getOracleType() {
		return "VARCHAR2";
	}

	@Override
	protected String writeDataValue(Object value) {
		String strValue = (String) value;
		strValue = strValue.replaceAll("'", "''").replaceAll("\r\n", "' || chr(13) || chr(10) ||'");
		return "'" + strValue + "'";
	}
}
