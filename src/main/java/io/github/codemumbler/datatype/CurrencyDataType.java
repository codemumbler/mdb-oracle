package io.github.codemumbler.datatype;

import java.math.BigDecimal;

public class CurrencyDataType extends PrecisionDataType {

	@Override
	public boolean hasPrecision() {
		return true;
	}

	@Override
	public int getDefaultPrecision() {
		return 2;
	}

	@Override
	public String writeValue(Object value) {
		BigDecimal money = (BigDecimal) value;
		return money.toPlainString();
	}
}
