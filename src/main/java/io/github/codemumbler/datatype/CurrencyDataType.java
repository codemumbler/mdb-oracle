package io.github.codemumbler.datatype;

public class CurrencyDataType extends PrecisionDataType {

	@Override
	public boolean hasPrecision() {
		return true;
	}

	@Override
	public int getDefaultPrecision() {
		return 2;
	}
}
