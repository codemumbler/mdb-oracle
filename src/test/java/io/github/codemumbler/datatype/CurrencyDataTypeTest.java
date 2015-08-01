package io.github.codemumbler.datatype;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class CurrencyDataTypeTest {

  @Test
  public void moneyValue() {
    CurrencyDataType currencyDataType = new CurrencyDataType();
    Assert.assertEquals("3.5", currencyDataType.writeValue(new BigDecimal(3.50D)));
  }
}
