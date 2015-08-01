package io.github.codemumbler.datatype;

import com.healthmarketscience.jackcess.Column;

public abstract class DataType {

  public int getLength(Column originalColumn) {
    return originalColumn.getLength();
  }

  public boolean hasLength() {
    return true;
  }

  public boolean hasPrecision() {
    return false;
  }

  public abstract String getOracleType();

  public final String writeValue(Object value) {
    if (value == null)
      return "NULL";
    return writeDataValue(value);
  }

  protected abstract String writeDataValue(Object value);

  public boolean isInsertable() {
    return true;
  }
}
