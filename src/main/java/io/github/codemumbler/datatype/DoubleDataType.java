package io.github.codemumbler.datatype;

public class DoubleDataType extends PrecisionDataType {

  @Override
  public boolean hasPrecision() {
    return true;
  }

  @Override
  public int getDefaultPrecision() {
    return 5;
  }
}
