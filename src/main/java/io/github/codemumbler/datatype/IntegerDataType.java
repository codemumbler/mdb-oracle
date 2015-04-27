package io.github.codemumbler.datatype;

import com.healthmarketscience.jackcess.Column;

public class IntegerDataType extends NumberDataType {

	@Override
	public int getLength(Column originalColumn) {
		return 5;
	}
}
