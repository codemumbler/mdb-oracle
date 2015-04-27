package io.github.codemumbler.datatype;

import com.healthmarketscience.jackcess.Column;

public abstract class DataType {

	public int getLength(Column originalColumn) {
		return originalColumn.getLength();
	}

	public boolean hasLength() {
		return true;
	}

	public boolean hasPrecision() {
		return false;
	}

	public abstract String getOracleType();
}
