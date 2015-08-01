package io.github.codemumbler.datatype;

import org.junit.Assert;
import org.junit.Test;

public class DataTypeFactoryTest {

  @Test
  public void nullObject() {
    DataTypeFactory factory = new DataTypeFactory();
    Assert.assertEquals(NullDataType.class, factory.createDataType(-1234).getClass());
  }
}
