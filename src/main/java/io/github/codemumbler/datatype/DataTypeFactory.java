package io.github.codemumbler.datatype;

public class DataTypeFactory {

  private static final int TEXT = 12;
  private static final int LONG_INTEGER = 4;
  private static final int MEMO = -1;
  private static final int INTEGER = 5;
  private static final int DATE_TIME = 93;
  private static final int DOUBLE = 8;
  private static final int BOOLEAN = 16;
  private static final int CURRENCY = 3;

  public DataType createDataType(int sqlType) {
    switch (sqlType) {
      case TEXT:
        return new Text();
      case INTEGER:
        return new IntegerDataType();
      case LONG_INTEGER:
        return new LongDataType();
      case MEMO:
        return new Memo();
      case DATE_TIME:
        return new DateTime();
      case DOUBLE:
        return new DoubleDataType();
      case BOOLEAN:
        return new BooleanDataType();
      case CURRENCY:
        return new CurrencyDataType();
      default:
        return new NullDataType();
    }
  }
}
