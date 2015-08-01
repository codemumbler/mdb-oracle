package io.github.codemumbler.datatype;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTime extends DataType {

  @Override
  public String getOracleType() {
    return "TIMESTAMP";
  }

  @Override
  public boolean hasLength() {
    return false;
  }

  @Override
  public String writeDataValue(Object value) {
    Date date = (Date) value;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    return "TO_TIMESTAMP('" + simpleDateFormat.format(date) + "', 'MM/DD/YYYY HH24:MI:SS')";
  }
}
