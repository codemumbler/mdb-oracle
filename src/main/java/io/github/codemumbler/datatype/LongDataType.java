package io.github.codemumbler.datatype;

import com.healthmarketscience.jackcess.Column;

public class LongDataType extends NumberDataType {

  @Override
  public int getLength(Column originalColumn) {
    return 9;
  }
}
