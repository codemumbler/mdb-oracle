package io.github.codemumbler.datatype;

public class NumberDataType extends DataType {

  @Override
  public String getOracleType() {
    return "NUMBER";
  }

  @Override
  public String writeDataValue(Object value) {
    return value.toString();
  }
}
