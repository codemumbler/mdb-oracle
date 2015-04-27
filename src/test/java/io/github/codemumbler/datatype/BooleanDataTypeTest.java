package io.github.codemumbler.datatype;

import org.junit.Assert;
import org.junit.Test;

public class BooleanDataTypeTest {

	@Test
	public void trueValue() {
		BooleanDataType booleanDataType = new BooleanDataType();
		Assert.assertEquals("'Y'", booleanDataType.writeValue(true));
	}

	@Test
	public void falseValue() {
		BooleanDataType booleanDataType = new BooleanDataType();
		Assert.assertEquals("'N'", booleanDataType.writeValue(false));
	}
}