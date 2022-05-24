package de.snx.psf.util;

public enum DataType {
	STRING(0), CHARACTER(1), BYTE(2), SHORT(3), INTEGER(4), LONG(5), FLOAT(6), DOUBLE(7), BOOLEAN(8);

	public final int ID;

	private DataType(int id) {
		this.ID = id;
	}
}
