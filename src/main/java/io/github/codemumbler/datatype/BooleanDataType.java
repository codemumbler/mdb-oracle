package io.github.codemumbler.datatype;

public class BooleanDataType extends DataType {

  @Override
  public String getOracleType() {
    return "VARCHAR2";
  }

  @Override
  public String writeDataValue(Object value) {
    return ((Boolean) value ? "'Y'" : "'N'");
  }
}
